package org.eman.runtime;

public class ReturnSignal extends RuntimeException {
    private final RuntimeValue value;

    public ReturnSignal(RuntimeValue value) {
        super(null, null, false, false);
        this.value = value != null ? value : RuntimeValue.nil();
    }

    public RuntimeValue getValue() {
        return value;
    }
}
