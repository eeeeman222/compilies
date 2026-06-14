package org.eman.semantic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SemanticEnvironment {
    private final SemanticEnvironment parent;
    private final Map<String, VariableInfo> variables = new HashMap<>();
    private final Map<String, FunctionInfo> functions = new HashMap<>();

    public SemanticEnvironment() {
        this(null);
    }

    public SemanticEnvironment(SemanticEnvironment parent) {
        this.parent = parent;
    }

    public boolean defineVariable(String name, boolean isInitialized, ValueType type) {
        if (variables.containsKey(name)) {
            return false;
        }
        variables.put(name, new VariableInfo(name, isInitialized, type));
        return true;
    }

    public boolean defineFunction(String name, int arity) {
        if (functions.containsKey(name) || variables.containsKey(name)) {
            return false;
        }
        functions.put(name, new FunctionInfo(name, arity));
        return true;
    }

    public boolean isFunctionDefined(String name) {
        if (functions.containsKey(name)) return true;
        return parent != null && parent.isFunctionDefined(name);
    }

    public int getFunctionArity(String name) {
        if (functions.containsKey(name)) return functions.get(name).arity;
        if (parent != null) return parent.getFunctionArity(name);
        return 0;
    }

    public void markVariableAsUsed(String name) {
        if (variables.containsKey(name)) {
            variables.get(name).used = true;
            return;
        }
        if (parent != null) parent.markVariableAsUsed(name);
    }

    public void markVariableAsInitialized(String name) {
        if (variables.containsKey(name)) {
            variables.get(name).initialized = true;
            return;
        }
        if (parent != null) parent.markVariableAsInitialized(name);
    }

    public void setVariableType(String name, ValueType type) {
        if (variables.containsKey(name)) {
            variables.get(name).type = type;
            return;
        }
        if (parent != null) parent.setVariableType(name, type);
    }

    public ValueType getVariableType(String name) {
        if (variables.containsKey(name)) return variables.get(name).type;
        if (parent != null) return parent.getVariableType(name);
        return ValueType.UNKNOWN;
    }

    public boolean isVariableDefined(String name) {
        if (variables.containsKey(name)) return true;
        return parent != null && parent.isVariableDefined(name);
    }

    public boolean isVariableInitialized(String name) {
        if (variables.containsKey(name)) return variables.get(name).initialized;
        if (parent != null) return parent.isVariableInitialized(name);
        return false;
    }

    public List<String> getUnusedVariables() {
        List<String> unused = new ArrayList<>();
        for (VariableInfo info : variables.values()) {
            if (!info.used) unused.add(info.name);
        }
        return unused;
    }

    public List<String> getUninitializedVariables() {
        List<String> uninitialized = new ArrayList<>();
        for (VariableInfo info : variables.values()) {
            if (!info.initialized) uninitialized.add(info.name);
        }
        return uninitialized;
    }

    private static class VariableInfo {
        private final String name;
        private boolean used;
        private boolean initialized;
        private ValueType type;

        VariableInfo(String name, boolean initialized, ValueType type) {
            this.name = name;
            this.initialized = initialized;
            this.type = type;
        }
    }

    private static class FunctionInfo {
        private final String name;
        private final int arity;

        FunctionInfo(String name, int arity) {
            this.name = name;
            this.arity = arity;
        }
    }
}
