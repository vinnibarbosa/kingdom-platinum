package com.br.pokefichas.commons.enums;

public interface EnumTypeValue<T extends EnumTypeValue<T, V>, V> extends Comparable<T> {

    V toValue();

    T fromValue(V var1);

    public static final class EnumTypeString {

        private EnumTypeString() {
        }

        public static <T extends EnumTypeValue<T, String>> T fromValue(
                final Class<T> type,
                final String dataValue,
                final boolean ignoreUnknown) {

            if (dataValue == null) {
                return null;
            }

            final EnumTypeValue[] enumValues = (EnumTypeValue[]) type.getEnumConstants();

            for (EnumTypeValue enumValue : enumValues) {
                @SuppressWarnings("unchecked")
                final T typedEnumValue = (T) enumValue;

                if (typedEnumValue.toValue().equals(dataValue)) {
                    return typedEnumValue;
                }
            }

            if (!ignoreUnknown) {
                throw new IllegalArgumentException("Unknown Enum for value [" + dataValue + "]");
            }
            return null;
        }

        public static <T extends EnumTypeValue<T, String>> T fromValue(
                final Class<T> type,
                final String dataValue) {
            return fromValue(type, dataValue, true);
        }
    }

    public static final class EnumTypeInteger {

        private EnumTypeInteger() {
        }

        public static <T extends EnumTypeValue<T, Integer>> T fromValue(
                final Class<T> type,
                final Integer dataValue,
                final boolean ignoreUnknown) {

            if (dataValue == null) {
                return null;
            }

            final EnumTypeValue[] enumValues = (EnumTypeValue[]) type.getEnumConstants();

            for (EnumTypeValue enumValue : enumValues) {
                @SuppressWarnings("unchecked")
                final T typedEnumValue = (T) enumValue;

                if (typedEnumValue.toValue().equals(dataValue)) {
                    return typedEnumValue;
                }
            }

            if (!ignoreUnknown) {
                throw new IllegalArgumentException("Unknown Enum for value [" + dataValue + "]");
            }

            return null;
        }

        public static <T extends EnumTypeValue<T, Integer>> T fromValue(
                final Class<T> type,
                final Integer dataValue) {
            return fromValue(type, dataValue, true);
        }
    }
}
