package reportRunner.Model.enums;

import lombok.Getter;

@Getter
public enum TestTypes {
    MAX_PERFORMANCE("Тест поиска максимальной производительности"),
    CONFIRM_MAX("Тест подтверждения максимальной производительности"),
    RELIABILITY("Тест надежности"),
    FAULT_TOLERANCE("Тест отказоустойчивости");

    private final String title;

    TestTypes(String title) {
        this.title = title;
    }

    public static TestTypes fromTitle(String title) {
        for (TestTypes types : values()) {
            if (types.getTitle().equalsIgnoreCase(title)) {
                return types;
            }
        }
        throw new IllegalArgumentException("Неверный тип теста: " + title);
    }
}
