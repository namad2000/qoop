package io.qoop.utils.api.enums;


import io.qoop.fault.handler.api.exception.DomainException;
import io.qoop.fault.handler.api.exception.ExceptionCode;

import java.lang.reflect.Method;
import java.util.*;

public final class EnumHelper {

    public static String enumPackage;

    private EnumHelper() {
    }

    @SuppressWarnings("unchecked")
    public static <E extends Enum<E>> E getEnumValueFromString(Class<?> enumClass, String value) {
        try {
            return Enum.valueOf((Class<E>) enumClass, value);
        } catch (ClassCastException | IllegalArgumentException e) {
            throw DomainException.of(ExceptionCode.BAD_REQUEST_ERROR);
        }
    }

    public static Object getEnumValuesAsJson(String enumClassName) {
        try {
            String fullClassName = enumPackage + "." + enumClassName;
            Class<?> clazz = Class.forName(fullClassName);
            if (!clazz.isEnum()) {
                throw DomainException.of(ExceptionCode.BAD_REQUEST_ERROR);
            }
            Method method = clazz.getMethod("obtainJsonList");
            return method.invoke(null);
        } catch (DomainException e) {
            throw e;
        } catch (Exception e) {
            throw DomainException.of(ExceptionCode.INTERNAL_ERROR);
        }
    }

    @SuppressWarnings("unchecked")
    public static <E extends Enum<E>> List<E> getEnumValues(Class<?> enumClass) {
        return Arrays.asList((E[]) enumClass.getEnumConstants());
    }

    public static <T extends Enum<T>> Map<String, Object> getEnumValueAsJson(String enumClassName, Integer ordinal) {
        try {
            String fullClassName = enumPackage + "." + enumClassName;
            Class<?> clazz = Class.forName(fullClassName);
            if (!clazz.isEnum()) {
                throw DomainException.of(ExceptionCode.BAD_REQUEST_ERROR);
            }
            @SuppressWarnings("unchecked")
            Class<T> enumClass = (Class<T>) clazz;
            T[] enumConstants = enumClass.getEnumConstants();
            if (ordinal < 0 || ordinal >= enumConstants.length) {
                throw DomainException.of(ExceptionCode.BAD_REQUEST_ERROR);
            }
            T enumConstant = enumConstants[ordinal];
            Map<String, Object> enumData = new HashMap<>();
            for (Method method : enumClass.getDeclaredMethods()) {
                if (method.getParameterCount() == 0 && method.getName().startsWith("get")) {
                    String fieldName = method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4);
                    Object value = method.invoke(enumConstant);
                    enumData.put(fieldName, value);
                }
            }
            return enumData;
        } catch (DomainException e) {
            throw e;
        } catch (Exception e) {
            throw DomainException.of(ExceptionCode.INTERNAL_ERROR);
        }
    }

    public static <E extends Enum<E>> Map<Integer, EnumKeyValue> getEnumKeyValueMap(Class<E> enumClass) {
        Map<Integer, EnumKeyValue> map = new LinkedHashMap<>();
        try {
            Method getDescription = enumClass.getMethod("getDescription");
            for (E constant : enumClass.getEnumConstants()) {
                String description = (String) getDescription.invoke(constant);
                map.put(constant.ordinal(), new EnumKeyValue(constant.name(), description));
            }
        } catch (Exception e) {
            throw DomainException.of(ExceptionCode.INTERNAL_ERROR);
        }
        return map;
    }
}