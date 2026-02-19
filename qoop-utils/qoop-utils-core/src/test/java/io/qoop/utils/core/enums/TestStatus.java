package io.qoop.utils.core.enums;

public enum TestStatus {
    ACTIVE(1, "فعال"),
    INACTIVE(0, "غیرفعال");

    private final Integer code;
    private final String description;

    TestStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static Object obtainJsonList() {
        return "[{\"code\":1, \"desc\":\"فعال\"}, {\"code\":0, \"desc\":\"غیرفعال\"}]";
    }
}