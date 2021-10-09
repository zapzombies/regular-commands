package io.github.zap.regularcommands.util;

import io.github.zap.regularcommands.commands.DefaultKeys;
import io.github.zap.regularcommands.converter.ArgumentConverter;
import io.github.zap.regularcommands.converter.ConversionResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

/**
 * Utility class containing built-in converters and helper methods.
 */
public final class Converters {
    public final static ArgumentConverter<BigDecimal> BIG_DECIMAL_CONVERTER = (form, argument) -> {
        try {
            return ConversionResult.of(true, new BigDecimal(argument), null);
        }
        catch(NumberFormatException e) {
            return ConversionResult.of(false, null, Component.translatable(DefaultKeys.ERROR_CONVERT_BIG_DECIMAL.key(),
                    Component.text(argument)));
        }
    };

    public final static ArgumentConverter<BigInteger> BIG_INTEGER_CONVERTER = (form, argument) -> {
        try {
            return ConversionResult.of(true, new BigInteger(argument), null);
        }
        catch(NumberFormatException e) {
            return ConversionResult.of(false, null, Component.translatable(DefaultKeys.ERROR_CONVERT_BIG_INTEGER.key(),
                    Component.text(argument)));
        }
    };

    public final static ArgumentConverter<Long> LONG_CONVERTER = (form, argument) -> {
        try {
            return ConversionResult.of(true, Long.parseLong(argument), null);
        }
        catch(NumberFormatException e) {
            return ConversionResult.of(false, null, Component.translatable(DefaultKeys.ERROR_CONVERT_LONG.key(),
                    Component.text(argument)));
        }
    };

    public final static ArgumentConverter<Integer> INTEGER_CONVERTER = (form, argument) -> {
        try {
            return ConversionResult.of(true, Integer.parseInt(argument), null);
        }
        catch(NumberFormatException e) {
            return ConversionResult.of(false, null, Component.translatable(DefaultKeys.ERROR_CONVERT_INTEGER.key(),
                    Component.text(argument)));
        }
    };

    public final static ArgumentConverter<Double> DOUBLE_CONVERTER = (form, argument) -> {
        try {
            return ConversionResult.of(true, Double.parseDouble(argument), null);
        }
        catch(NumberFormatException e) {
            return ConversionResult.of(false, null, Component.translatable(DefaultKeys.ERROR_CONVERT_DOUBLE.key(),
                    Component.text(argument)));
        }
    };

    public final static ArgumentConverter<Float> FLOAT_CONVERTER = (form, argument) -> {
        try {
            return ConversionResult.of(true, Float.parseFloat(argument), null);
        }
        catch(NumberFormatException e) {
            return ConversionResult.of(false, null, Component.translatable(DefaultKeys.ERROR_CONVERT_FLOAT.key(),
                    Component.text(argument)));
        }
    };

    public final static ArgumentConverter<Short> SHORT_CONVERTER = (form, argument) -> {
        try {
            return ConversionResult.of(true, Short.parseShort(argument), null);
        }
        catch(NumberFormatException e) {
            return ConversionResult.of(false, null, Component.translatable(DefaultKeys.ERROR_CONVERT_SHORT.key(),
                    Component.text(argument)));
        }
    };

    public final static ArgumentConverter<Byte> BYTE_CONVERTER = (form, argument) -> {
        try {
            return ConversionResult.of(true, Byte.parseByte(argument), null);
        }
        catch(NumberFormatException e) {
            return ConversionResult.of(false, null, Component.translatable(DefaultKeys.ERROR_CONVERT_BYTE.key(),
                    Component.text(argument)));
        }
    };

    public final static ArgumentConverter<Boolean> BOOLEAN_CONVERTER = (form, argument) -> {
        try {
            return ConversionResult.of(true, parseBoolean(argument), null);
        }
        catch (NumberFormatException e) {
            return ConversionResult.of(false, null, Component.translatable(DefaultKeys.ERROR_CONVERT_BOOLEAN.key(),
                    Component.text(argument)));
        }
    };

    public static final ArgumentConverter<Material> MATERIAL_CONVERTER = (form, argument) -> {
        Material material = Material.getMaterial(argument);

        if(material != null) {
            return ConversionResult.of(true, material, null);
        }

        return ConversionResult.of(false, null, Component.translatable(DefaultKeys.ERROR_CONVERT_MATERIAL.key(),
                Component.text(argument)));
    };

    /**
     * Creates an ArgumentConverter that can convert an input sequence into an array, given an ArgumentConverter
     * that is capable of converting individual arguments, and a delimiter to split the input string.
     * @param elementConverter The converter that will convert each element
     * @param delimiter The delimiter used to split up the input string
     * @param arrayType The type of the array
     * @param <T> The type of argument we're trying to convert
     * @return An argument converter capable of transforming an input string into an array
     */
    public static <T> ArgumentConverter<T[]> newArrayConverter(ArgumentConverter<T> elementConverter, String delimiter,
                                                               Class<T> arrayType) {
        Objects.requireNonNull(elementConverter, "element converter cannot be null");
        Objects.requireNonNull(delimiter, "delimiter cannot be null");
        Objects.requireNonNull(arrayType, "arrayType cannot be null");

        return (form, argument) -> {
            String[] components = argument.split(delimiter);
            if(components.length == 0) {
                //noinspection unchecked
                return ConversionResult.of(true, (T[])Array.newInstance(arrayType, 0), null);
            }

            //noinspection unchecked
            T[] resultingArray = (T[])Array.newInstance(arrayType, components.length);

            for(int i = 0; i < components.length; i++) {
                String component = components[i];
                ConversionResult<T> result = elementConverter.convert(form, component);

                if(result.isValid()) {
                    resultingArray[i] = result.getConversion();
                }
                else {
                    return ConversionResult.of(false, null, result.getErrorMessage());
                }
            }

            return ConversionResult.of(true, resultingArray, null);
        };
    }

    /**
     * Converts a string to a boolean, case ignored. Throws a NumberFormatException if the conversion fails.
     * @param value The input string
     * @return true if value.equalsIgnoreCase("true"), false if value.equalsIgnoreCase("false")
     */
    private static boolean parseBoolean(String value) {
        if(value.equalsIgnoreCase("true")) {
            return true;
        }
        else if(value.equalsIgnoreCase("false")) {
            return false;
        }
        else {
            throw new NumberFormatException();
        }
    }
}