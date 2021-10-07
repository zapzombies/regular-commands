package io.github.zap.regularcommands.converter;

import io.github.zap.regularcommands.commands.CommandForm;

/**
 * Data class used internally to store the result of testing a CommandForm against a user input string.
 */
public class MatchResult {
    private final CommandForm<?> form;
    private final boolean hasPermission;
    private final boolean matches;
    private final ConversionResult<Object[]> conversionResult;

    /**
     * Creates a new MatchResult object from the specified data
     * @param form The associated CommandForm
     * @param hasPermission Whether or not the user has permission to execute this form
     * @param matches Whether or not the form's signature matches the input
     * @param conversionResult The result of converting every input argument
     */
    public MatchResult(CommandForm<?> form, boolean hasPermission, boolean matches, ConversionResult<Object[]> conversionResult) {
        this.form = form;
        this.hasPermission = hasPermission;
        this.matches = matches;
        this.conversionResult = conversionResult;
    }

    /**
     * Gets the form associated with this MatchResult.
     * @return The associated form
     */
    public CommandForm<?> getForm() {
        return form;
    }

    /**
     * Returns whether or not the match was successful.
     * @return True if the command form's signature matches the provided arguments, false otherwise
     */
    public boolean matches() {
        return matches;
    }

    /**
     * Returns the result of attempting to convert every input argument. This value may be null if no conversion
     * was performed as a result of the caller having insufficient permissions.
     * @return The result of converting the input arguments, or null
     */
    public ConversionResult<Object[]> getConversionResult() {
        return conversionResult;
    }

    /**
     * Whether or not the invoking player actually has permission to execute this form.
     * @return True if they have permission, false otherwise
     */
    public boolean hasPermission() {
        return hasPermission;
    }
}