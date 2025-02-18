package ru.sterkhovkv.space_app.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CalculationMode {
    SGP4("SGP4"),
    KEPLER_NEWTON("KeplerNewton");

    private final String value;

    public static CalculationMode fromValue(String value) {
        for (CalculationMode type : CalculationMode.values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        return null;
    }
}
