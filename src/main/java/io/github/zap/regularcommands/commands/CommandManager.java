package io.github.zap.regularcommands.commands;

import io.github.zap.regularcommands.converter.ConversionResult;
import io.github.zap.regularcommands.converter.MatchResult;
import io.github.zap.regularcommands.util.ArrayUtils;
import io.github.zap.regularcommands.validator.CommandValidator;
import io.github.zap.regularcommands.validator.ValidationResult;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.*;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * This class keeps track of all registered commands and includes some utility functions.
 */
public class CommandManager implements CommandExecutor, TabCompleter {
    public enum ErrorType {
        CONVERSION,
        PERMISSIONS,
        FORMS
    }

    private static final Key TRANSLATION_REGISTRY_KEY
            = Key.key("io.github.zap", "regularcommands.translation.registry");

    private static final Locale DEFAULT_LOCALE = Locale.US;
    private static final Function<Component, Component> DEFAULT_ERROR_FORMAT =
            (component -> component.color(TextColor.color(0xFF, 0x2A, 0x0)));

    private class SimpleCommand extends RegularCommand {
        private SimpleCommand(String name) {
            super(CommandManager.this, name, new BasicPageBuilder());
        }
    }

    private final Plugin plugin;
    private final TranslationRegistry translationRegistry;
    private final Logger logger;
    private final Map<String, RegularCommand> commands;

    private Function<Component, Component> conversionErrFormat = DEFAULT_ERROR_FORMAT;
    private Function<Component, Component> permissionsErrFormat = DEFAULT_ERROR_FORMAT;
    private Function<Component, Component> formErrFormat = DEFAULT_ERROR_FORMAT;

    private final StringBuilder BUFFER = new StringBuilder(); //used for internal string parsing

    /**
     * Creates a new CommandManager and associates it with the specified plugin.
     * @param plugin The associated plugin
     */
    public CommandManager(@NotNull Plugin plugin, @NotNull GlobalTranslator translator) {
        this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");
        this.translationRegistry = TranslationRegistry.create(TRANSLATION_REGISTRY_KEY);
        translationRegistry.defaultLocale(DEFAULT_LOCALE);
        translator.addSource(translationRegistry);
        logger = plugin.getLogger();
        commands = new HashMap<>();
    }

    public void setDefaultFormat(@NotNull ErrorType type, @NotNull Function<Component, Component> function) {
        Objects.requireNonNull(type, "type cannot be null");
        Objects.requireNonNull(function, "function cannot be null");

        switch (type) {
            case FORMS -> formErrFormat = function;
            case CONVERSION -> conversionErrFormat = function;
            case PERMISSIONS -> permissionsErrFormat = function;
        }
    }

    /**
     * Registers the default translations for all keys stored in {@link DefaultKeys}. If translations have already been
     * registered for the default locale, the previous mappings will not be overwritten. This is mostly a convenience
     * method; it is preferred that implementations load their own mappings from a language file or other data source.
     */
    public void registerDefaultTranslations() {
        for(DefaultKeys key : DefaultKeys.values()) {
            try {
                translationRegistry.register(key.key(), DEFAULT_LOCALE, new MessageFormat(key.getPattern(), DEFAULT_LOCALE));
            }
            catch (IllegalArgumentException ignored) {}
        }
    }

    /**
     * Registers a RegularCommand with this manager.
     * @param command The RegularCommand to register
     */
    public void registerCommand(@NotNull RegularCommand command) {
        String name = Objects.requireNonNull(command, "command cannot be null").getName();

        if(!commands.containsKey(name)) {
            commands.put(name, command);
            PluginCommand pluginCommand = Objects.requireNonNull(plugin.getServer().getPluginCommand(command.getName()),
                    "command must also be defined in plugin.yml");
            pluginCommand.setExecutor(this);
            pluginCommand.setTabCompleter(this);
        }
        else {
            throw new IllegalArgumentException("a command with that name has already been registered");
        }
    }

    /**
     * Returns the RegularCommand with the specified name.
     * @param name The name of the command
     * @return The RegularCommand with the given unique name, or null if it doesn't exist
     */
    public RegularCommand getCommand(@NotNull String name) {
        return commands.get(name);
    }

    /**
     * Returns if a RegularCommand with the given name has been registered.
     * @param name The name of the command
     * @return if it has been registered
     */
    public boolean hasCommand(@NotNull String name) {
        return commands.containsKey(name);
    }

    /**
     * Returns the JavaPlugin this CommandManager instance is attached to.
     * @return The associated JavaPlugin
     */
    public @NotNull Plugin getPlugin() {
        return plugin;
    }

    /**
     * Returns the TranslationRegistry instance used by this CommandManager.
     */
    public @NotNull TranslationRegistry getTranslationRegistry() {
        return translationRegistry;
    }

    /**
     * Returns the logger used by this instance, which is the same logger that is used by the bound JavaPlugin.
     * @return The associated Logger
     */
    public @NotNull Logger getLogger() { return logger; }

    /**
     * Registers a CommandForm with this CommandManager. A default implementation of RegularCommand will be created if
     * one with the given name is absent; if the name exists, the form will be added to the already-present command.
     * Note that it is still necessary to have a command matching the given name defined in plugin.yml.
     * @param name The name of the command to register the form under
     * @param form The CommandForm instance to register
     */
    public void registerForm(@NotNull String name, @NotNull CommandForm<?> form) {
        commands.computeIfAbsent(name, SimpleCommand::new).addForm(form);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        RegularCommand regularCommand = getCommand(command.getName());

        if(regularCommand != null) {
            List<MatchResult> matches = regularCommand.getMatches(parse(args), commandSender); //get all matches

            if(matches.size() > 0) {
                for(MatchResult match : matches) { //loop all matches
                    if(match.hasPermission()) { //check permissions match first
                        ConversionResult<Object[]> conversionResult = match.getConversionResult();

                        if(conversionResult.isValid()) { //conversion was a success
                            CommandForm<?> form = match.getForm();
                            Component output = validateAndExecute(form, commandSender, conversionResult.getConversion());

                            if(output != null && !output.equals(Component.empty())) { //we have something to display
                                commandSender.sendMessage(output);
                            }
                        }
                        else { //conversion error
                            commandSender.sendMessage(conversionErrFormat.apply(conversionResult.getErrorMessage()));
                        }
                    }
                    else { //sender does not have the required permissions
                        commandSender.sendMessage(permissionsErrFormat.apply(
                                Component.translatable(DefaultKeys.ERROR_NO_PERMISSION.key())));
                    }
                }
            }
            else { //no matching forms
                commandSender.sendMessage(formErrFormat.apply(Component.translatable(DefaultKeys.ERROR_NO_FORMS.key())
                        .color(TextColor.color(0xFF, 0x2A, 0x0))));
            }
        }
        else {
            getLogger().warning(String.format("CommandSender '%s' tried to execute command '%s', which should not be " +
                    "possible due to it not being present in the command map.", commandSender.getName(),
                    command.getName()));
        }

        return true;
    }

    private <T> Component validateAndExecute(CommandForm<T> form, CommandSender sender, Object[] args) {
        Context context = new Context(form, sender);
        CommandValidator<T, ?> validator = form.getValidator(context, args);

        if(validator != null) {
            ValidationResult<T> result = validator.validate(context, args);

            if(result.isValid()) {
                return form.execute(context, args, result.getData());
            }
            else {
                sender.sendMessage(result.getErrorMessage());
            }
        }
        else {
            return form.execute(context, args, null);
        }

        return null;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command,
                                      @NotNull String label, @NotNull String[] args) {
        if(args.length > 0) { //possibly redundant, needs testing
            RegularCommand regularCommand = commands.get(command.getName());

            if(regularCommand != null) {
                return regularCommand.getCompletions(commandSender, parse(args));
            }
        }

        return new ArrayList<>();
    }

    private String[] parse(String[] args) {
        BUFFER.setLength(0);

        List<String> result = new ArrayList<>();
        boolean quotation = false;
        int lastOpeningQuoteResult = 0;
        int lastOpeningQuoteArg = 0;
        int resultIndex = 0;
        int argIndex = 0;
        for(String arg : args) {
            if(quotation) {
                BUFFER.append(' ');

                if(arg.endsWith("\"")) {
                    quotation = false;
                    BUFFER.append(arg.length() == 1 ? StringUtils.EMPTY : arg.substring(0, arg.length() - 1));
                    result.add(BUFFER.toString());
                    BUFFER.setLength(0);
                    resultIndex++;
                }
                else {
                    BUFFER.append(arg);
                }
            }
            else {
                if(arg.startsWith("\"")) {
                    quotation = true;
                    BUFFER.append(arg.length() == 1 ? StringUtils.EMPTY : arg.substring(1));
                    lastOpeningQuoteResult = resultIndex;
                    lastOpeningQuoteArg = argIndex;
                }
                else {
                    result.add(arg);
                    resultIndex++;
                }
            }
            argIndex++;
        }

        if(BUFFER.length() > 0) {
            if (result.size() > lastOpeningQuoteResult) {
                result.subList(lastOpeningQuoteResult, result.size()).clear();
            }

            result.addAll(Arrays.asList(args).subList(lastOpeningQuoteArg, args.length));
        }

        return result.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
    }
}