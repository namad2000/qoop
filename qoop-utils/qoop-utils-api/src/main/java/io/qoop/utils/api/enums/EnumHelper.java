package io.qoop.utils.api.enums;


import java.util.List;
import java.util.Map;

public interface EnumHelper {

    <E extends Enum<E>> E getEnumValueFromString(Class<?> enumClass, String value);

    Object getEnumValuesAsJson(String enumClassName);

    <E extends Enum<E>> List<E> getEnumValues(Class<?> enumClass);

    <T extends Enum<T>> Map<String, Object> getEnumValueAsJson(String enumClassName, Integer ordinal);

    <E extends Enum<E>> Map<Integer, EnumKeyValue> getEnumKeyValueMap(Class<E> enumClass);
}