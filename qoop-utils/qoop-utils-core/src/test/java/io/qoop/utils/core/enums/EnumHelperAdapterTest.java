package io.qoop.utils.core.enums;


import io.qoop.fault.handler.api.exception.DomainException;
import io.qoop.fault.handler.api.exception.ExceptionCode;
import io.qoop.utils.api.enums.EnumKeyValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EnumHelperAdapterTest {

    private EnumHelperAdapter enumHelperAdapter;

    @BeforeEach
    void setUp() {
        enumHelperAdapter = new EnumHelperAdapter();
        enumHelperAdapter.setEnumPackage("io.qoop.utils.core.enums");
    }

    @Test
    void testGetEnumValueFromString_Success() {
        String value = "ACTIVE";
        TestStatus result = enumHelperAdapter.getEnumValueFromString(TestStatus.class, value);
        assertEquals(TestStatus.ACTIVE, result);
    }

    @Test
    void testGetEnumValueFromString_InvalidValue_ShouldThrowException() {
        String invalidValue = "PENDING";
        DomainException exception = assertThrows(DomainException.class, () -> {
            enumHelperAdapter.getEnumValueFromString(TestStatus.class, invalidValue);
        });
        assertEquals(ExceptionCode.BAD_REQUEST_ERROR, exception.getCode());
    }

    @Test
    void testGetEnumValues_Success() {
        List<TestStatus> result = enumHelperAdapter.getEnumValues(TestStatus.class);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(TestStatus.ACTIVE));
        assertTrue(result.contains(TestStatus.INACTIVE));
    }

    @Test
    void testGetEnumValueAsJson_Success() {
        Map<String, Object> result = enumHelperAdapter.getEnumValueAsJson("TestStatus", 0);
        assertNotNull(result);
        assertEquals(1, result.get("code"));
        assertEquals("فعال", result.get("description"));
    }

    @Test
    void testGetEnumValueAsJson_InvalidOrdinal_ShouldThrowException() {
        DomainException exception = assertThrows(DomainException.class, () -> {
            enumHelperAdapter.getEnumValueAsJson("TestStatus", 99);
        });
        assertEquals(ExceptionCode.BAD_REQUEST_ERROR, exception.getCode());
    }

    @Test
    void testGetEnumKeyValueMap_Success() {
        Map<Integer, EnumKeyValue> result = enumHelperAdapter.getEnumKeyValueMap(TestStatus.class);
        assertNotNull(result);
        assertEquals(2, result.size());
        EnumKeyValue activeKeyValue = result.get(0);
        assertEquals("ACTIVE", activeKeyValue.key());
        assertEquals("فعال", activeKeyValue.value());
    }

    @Test
    void testGetEnumValuesAsJson_Success() {
        Object result = enumHelperAdapter.getEnumValuesAsJson("TestStatus");
        assertNotNull(result);
        assertTrue(result instanceof String);
        assertTrue(((String) result).contains("فعال"));
    }
}