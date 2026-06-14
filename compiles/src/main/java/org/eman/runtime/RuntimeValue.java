package org.eman.runtime;

import org.eman.semantic.ValueType;

import java.util.ArrayList;
import java.util.List;

public final class RuntimeValue {
    private final ValueType type;
    private final Object value;

    private RuntimeValue(ValueType type, Object value) {
        this.type = type;
        this.value = value;
    }

    public static RuntimeValue number(double n) {
        return new RuntimeValue(ValueType.NUMBER, n);
    }

    public static RuntimeValue string(String s) {
        return new RuntimeValue(ValueType.STRING, s);
    }

    public static RuntimeValue bool(boolean b) {
        return new RuntimeValue(ValueType.BOOLEAN, b);
    }

    public static RuntimeValue array(List<RuntimeValue> items) {
        return new RuntimeValue(ValueType.ARRAY, new ArrayList<>(items));
    }

    public static RuntimeValue nil() {
        return new RuntimeValue(ValueType.NIL, null);
    }

    public ValueType getType() {
        return type;
    }

    public double asNumber() {
        return (Double) value;
    }

    public String asString() {
        return (String) value;
    }

    public boolean asBoolean() {
        return (Boolean) value;
    }

    @SuppressWarnings("unchecked")
    public List<RuntimeValue> asArray() {
        return (List<RuntimeValue>) value;
    }

    public boolean isTruthy() {
        return switch (type) {
            case NIL -> false;
            case BOOLEAN -> asBoolean();
            case NUMBER -> asNumber() != 0;
            case STRING -> !asString().isEmpty();
            case ARRAY -> !asArray().isEmpty();
            default -> true;
        };
    }

    @Override
    public String toString() {
        return switch (type) {
            case NUMBER -> {
                double n = asNumber();
                if (n == (long) n) yield String.valueOf((long) n);
                yield String.valueOf(n);
            }
            case STRING -> asString();
            case BOOLEAN -> String.valueOf(asBoolean());
            case ARRAY -> asArray().toString();
            case NIL -> "nil";
            default -> "?";
        };
    }
}
