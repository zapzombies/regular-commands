package io.github.zap.regularcommands.commands;

import org.jetbrains.annotations.NotNull;

public enum DefaultKeys {
    NAVBAR("default.feedback.page.navbar", "-==Page {0}/{1}==-"),
    ERROR_PAGE_INDEX_OUT_OF_BOUNDS("default.feedback.error.command.help.index_out_of_bounds", "Index {0} out of bounds!"),
    ERROR_ENTITY_EXECUTOR("default.feedback.error.entity_executor", "Only entities can execute this command!"),
    ERROR_PLAYER_EXECUTOR("default.feedback.error.player_executor", "Only players can execute this command!"),
    ERROR_CONSOLE_EXECUTOR("default.feedback.error.console_executor", "Only the console can execute this command!"),
    ERROR_BLOCK_EXECUTOR("default.feedback.error.block_executor", "Only blocks can execute this command!"),
    ERROR_NO_PERMISSION("default.feedback.error.no_permission", "You do not have permission to execute this command!"),
    ERROR_NO_FORMS("default.feedback.error.no_forms", "No matching forms exist for that command!"),
    ERROR_CONVERT_BIG_DECIMAL("default.feedback.error.convert.big_decimal", "Value {0} cannot be converted to a BigDecimal!"),
    ERROR_CONVERT_BIG_INTEGER("default.feedback.error.convert.big_integer", "Value {0} cannot be converted to a BigInteger!"),
    ERROR_CONVERT_LONG("default.feedback.error.convert.long", "Value {0} cannot be converted to a long!"),
    ERROR_CONVERT_INTEGER("default.feedback.error.convert.integer", "Value {0} cannot be converted to an int!"),
    ERROR_CONVERT_DOUBLE("default.feedback.error.convert.double", "Value {0} cannot be converted to a double!"),
    ERROR_CONVERT_FLOAT("default.feedback.error.convert.float", "Value {0} cannot be converted to a float!"),
    ERROR_CONVERT_SHORT("default.feedback.error.convert.short", "Value {0} cannot be converted to a short!"),
    ERROR_CONVERT_BYTE("default.feedback.error.convert.byte", "Value {0} cannot be converted to a byte!"),
    ERROR_CONVERT_BOOLEAN("default.feedback.error.convert.boolean", "Value {0} cannot be converted to a boolean!"),
    ERROR_CONVERT_MATERIAL("default.feedback.error.convert.material","Value {0} cannot be converted to a Material!"),
    COMMAND_HELP_PARAM_1_USAGE("default.command.help.usage.param.1", "[help]");

    private final String key;
    private final String defaultPattern;

    DefaultKeys(String key, String defaultPattern) {
        this.key = key;
        this.defaultPattern = defaultPattern;
    }

    public @NotNull String key() {
        return key;
    }

    public @NotNull String getPattern() {
        return defaultPattern;
    }
}
