package org.eman.semantic;

import org.eman.parser.AstVisitor;
import org.eman.parser.ast.*;
import org.eman.lexer.TokenType;

import java.util.ArrayList;
import java.util.List;

public class SemanticAnalyzer implements AstVisitor<ValueType> {
    private SemanticEnvironment environment = new SemanticEnvironment();
    private final List<String> errors = new ArrayList<>();

    public void analyze(List<Statement> statements) {
        for (Statement statement : statements) {
            statement.accept(this);
        }
        checkUnusedVariables();
        checkUninitializedVariables();
    }

    private void checkUnusedVariables() {
        for (String varName : environment.getUnusedVariables()) {
            errors.add("Warning: Variable '" + varName + "' is defined but never used.");
        }
    }

    private void checkUninitializedVariables() {
        for (String varName : environment.getUninitializedVariables()) {
            errors.add("Warning: Variable '" + varName + "' is defined but never initialized.");
        }
    }

    @Override
    public ValueType visitVarStatement(VarStatement statement) {
        ValueType initType = ValueType.UNKNOWN;
        if (statement.hasInitializer()) {
            initType = statement.getInitializer().accept(this);
        }

        if (!environment.defineVariable(statement.getName(), statement.hasInitializer(), initType)) {
            errors.add("Variable '" + statement.getName() + "' is already defined.");
        }
        return ValueType.NIL;
    }

    @Override
    public ValueType visitPrintStatement(PrintStatement statement) {
        statement.getExpression().accept(this);
        return ValueType.NIL;
    }

    @Override
    public ValueType visitExpressionStatement(ExpressionStatement statement) {
        statement.getExpression().accept(this);
        return ValueType.NIL;
    }

    @Override
    public ValueType visitBlockStatement(BlockStatement statement) {
        SemanticEnvironment previous = environment;
        environment = new SemanticEnvironment(previous);

        for (Statement inner : statement.getStatements()) {
            inner.accept(this);
        }

        for (String varName : environment.getUnusedVariables()) {
            errors.add("Warning: Variable '" + varName + "' is defined in block but never used.");
        }
        for (String varName : environment.getUninitializedVariables()) {
            errors.add("Warning: Variable '" + varName + "' is defined in block but never initialized.");
        }

        environment = previous;
        return ValueType.NIL;
    }

    @Override
    public ValueType visitIfStatement(IfStatement statement) {
        ValueType condType = statement.getCondition().accept(this);
        if (condType != ValueType.BOOLEAN && condType != ValueType.UNKNOWN && condType != ValueType.NUMBER) {
            errors.add("If condition must be boolean, got " + condType + ".");
        }
        statement.getThenBranch().accept(this);
        if (statement.getElseBranch() != null) {
            statement.getElseBranch().accept(this);
        }
        return ValueType.NIL;
    }

    @Override
    public ValueType visitWhileStatement(WhileStatement statement) {
        statement.getCondition().accept(this);
        statement.getBody().accept(this);
        return ValueType.NIL;
    }

    @Override
    public ValueType visitFunctionStatement(FunctionStatement statement) {
        if (!environment.defineFunction(statement.getName(), statement.getParams().size())) {
            errors.add("Function '" + statement.getName() + "' is already defined.");
        }

        SemanticEnvironment previous = environment;
        environment = new SemanticEnvironment(previous);

        for (String param : statement.getParams()) {
            environment.defineVariable(param, true, ValueType.UNKNOWN);
        }

        for (Statement inner : statement.getBody()) {
            inner.accept(this);
        }

        environment = previous;
        return ValueType.NIL;
    }

    @Override
    public ValueType visitReturnStatement(ReturnStatement statement) {
        if (statement.hasValue()) {
            statement.getValue().accept(this);
        }
        return ValueType.NIL;
    }

    @Override
    public ValueType visitNumberExpression(NumberExpression expression) {
        return ValueType.NUMBER;
    }

    @Override
    public ValueType visitStringExpression(StringExpression expression) {
        return ValueType.STRING;
    }

    @Override
    public ValueType visitVariableExpression(VariableExpression expression) {
        if (!environment.isVariableDefined(expression.getName())) {
            if (!environment.isFunctionDefined(expression.getName())) {
                errors.add("Variable '" + expression.getName() + "' is not defined.");
            }
            return ValueType.UNKNOWN;
        }

        if (!environment.isVariableInitialized(expression.getName())) {
            errors.add("Variable '" + expression.getName() + "' may be used before initialization.");
        }

        environment.markVariableAsUsed(expression.getName());
        return environment.getVariableType(expression.getName());
    }

    @Override
    public ValueType visitBinaryExpression(BinaryExpression expression) {
        ValueType left = expression.getLeft().accept(this);
        ValueType right = expression.getRight().accept(this);

        return switch (expression.getOperator()) {
            case PLUS -> {
                if (left == ValueType.STRING || right == ValueType.STRING) yield ValueType.STRING;
                if (left != ValueType.NUMBER && left != ValueType.UNKNOWN) {
                    errors.add("Type mismatch: '+' expects numbers or strings.");
                }
                yield ValueType.NUMBER;
            }
            case MINUS, STAR, SLASH -> {
                if (left != ValueType.NUMBER && left != ValueType.UNKNOWN
                        || right != ValueType.NUMBER && right != ValueType.UNKNOWN) {
                    errors.add("Type mismatch: arithmetic operator expects numbers.");
                }
                yield ValueType.NUMBER;
            }
            case EQEQ, NEQ, LT, GT, LTEQ, GTEQ, AND, OR -> ValueType.BOOLEAN;
            default -> ValueType.UNKNOWN;
        };
    }

    @Override
    public ValueType visitUnaryExpression(UnaryExpression expression) {
        expression.getRight().accept(this);
        return expression.getOperator() == TokenType.EXCL ? ValueType.BOOLEAN : ValueType.NUMBER;
    }

    @Override
    public ValueType visitAssignExpression(AssignExpression expression) {
        ValueType valueType = expression.getValue().accept(this);

        if (!environment.isVariableDefined(expression.getName())) {
            errors.add("Variable '" + expression.getName() + "' is not defined.");
        } else {
            environment.markVariableAsInitialized(expression.getName());
            environment.markVariableAsUsed(expression.getName());
            environment.setVariableType(expression.getName(), valueType);
        }

        return valueType;
    }

    @Override
    public ValueType visitCallExpression(CallExpression expression) {
        if (!environment.isFunctionDefined(expression.getName())) {
            errors.add("Function '" + expression.getName() + "' is not defined.");
            for (Expression arg : expression.getArguments()) {
                arg.accept(this);
            }
            return ValueType.UNKNOWN;
        }

        int expected = environment.getFunctionArity(expression.getName());
        if (expected != expression.getArguments().size()) {
            errors.add("Function '" + expression.getName() + "' expects "
                    + expected + " arguments, got " + expression.getArguments().size() + ".");
        }

        for (Expression arg : expression.getArguments()) {
            arg.accept(this);
        }

        return ValueType.UNKNOWN;
    }

    @Override
    public ValueType visitArrayExpression(ArrayExpression expression) {
        for (Expression element : expression.getElements()) {
            element.accept(this);
        }
        return ValueType.ARRAY;
    }

    @Override
    public ValueType visitIndexExpression(IndexExpression expression) {
        ValueType arrayType = expression.getArray().accept(this);
        ValueType indexType = expression.getIndex().accept(this);

        if (arrayType != ValueType.ARRAY && arrayType != ValueType.UNKNOWN) {
            errors.add("Index operator requires an array.");
        }
        if (indexType != ValueType.NUMBER && indexType != ValueType.UNKNOWN) {
            errors.add("Array index must be a number.");
        }

        return ValueType.UNKNOWN;
    }

    @Override
    public ValueType visitIndexAssignExpression(IndexAssignExpression expression) {
        expression.getArray().accept(this);
        expression.getIndex().accept(this);
        return expression.getValue().accept(this);
    }

    public List<String> getErrors() {
        return errors;
    }
}
