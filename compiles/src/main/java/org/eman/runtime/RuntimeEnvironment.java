package org.eman.runtime;

import org.eman.parser.ast.FunctionStatement;

import java.util.HashMap;
import java.util.Map;

public class RuntimeEnvironment {
    private final RuntimeEnvironment parent;
    private final Map<String, RuntimeValue> variables = new HashMap<>();
    private final Map<String, FunctionStatement> functions = new HashMap<>();

    public RuntimeEnvironment() {
        this(null);
    }

    public RuntimeEnvironment(RuntimeEnvironment parent) {
        this.parent = parent;
    }

    public void define(String name, RuntimeValue value) {
        variables.put(name, value);
    }

    public void assign(String name, RuntimeValue value) {
        if (variables.containsKey(name)) {
            variables.put(name, value);
            return;
        }
        if (parent != null) {
            parent.assign(name, value);
            return;
        }
        throw new RuntimeException("Undefined variable '" + name + "'.");
    }

    public RuntimeValue get(String name) {
        if (variables.containsKey(name)) {
            return variables.get(name);
        }
        if (parent != null) {
            return parent.get(name);
        }
        throw new RuntimeException("Undefined variable '" + name + "'.");
    }

    public void defineFunction(FunctionStatement function) {
        functions.put(function.getName(), function);
    }

    public FunctionStatement getFunction(String name) {
        if (functions.containsKey(name)) {
            return functions.get(name);
        }
        if (parent != null) {
            return parent.getFunction(name);
        }
        throw new RuntimeException("Undefined function '" + name + "'.");
    }
}
