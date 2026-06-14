package org.eman.runtime;

import org.eman.lexer.TokenType;
import org.eman.parser.AstVisitor;
import org.eman.parser.ast.*;
import org.eman.semantic.ValueType;

import java.util.ArrayList;
import java.util.List;

public class Interpreter implements AstVisitor<RuntimeValue> {
    private RuntimeEnvironment environment = new RuntimeEnvironment();

    public void interpret(List<Statement> statements) {
        for (Statement statement : statements) {
            execute(statement);
        }
    }

    private void execute(Statement statement) {
        statement.accept(this);
    }

    @Override
    public RuntimeValue visitVarStatement(VarStatement statement) {
        RuntimeValue value = statement.hasInitializer()
                ? statement.getInitializer().accept(this)
                : RuntimeValue.nil();
        environment.define(statement.getName(), value);
        return RuntimeValue.nil();
    }

    @Override
    public RuntimeValue visitPrintStatement(PrintStatement statement) {
        System.out.println(statement.getExpression().accept(this));
        return RuntimeValue.nil();
    }

    @Override
    public RuntimeValue visitExpressionStatement(ExpressionStatement statement) {
        statement.getExpression().accept(this);
        return RuntimeValue.nil();
    }

    @Override
    public RuntimeValue visitBlockStatement(BlockStatement statement) {
        RuntimeEnvironment previous = environment;
        environment = new RuntimeEnvironment(previous);

        try {
            for (Statement inner : statement.getStatements()) {
                execute(inner);
            }
        } finally {
            environment = previous;
        }

        return RuntimeValue.nil();
    }

    @Override
    public RuntimeValue visitIfStatement(IfStatement statement) {
        if (statement.getCondition().accept(this).isTruthy()) {
            execute(statement.getThenBranch());
        } else if (statement.getElseBranch() != null) {
            execute(statement.getElseBranch());
        }
        return RuntimeValue.nil();
    }

    @Override
    public RuntimeValue visitWhileStatement(WhileStatement statement) {
        while (statement.getCondition().accept(this).isTruthy()) {
            execute(statement.getBody());
        }
        return RuntimeValue.nil();
    }

    @Override
    public RuntimeValue visitFunctionStatement(FunctionStatement statement) {
        environment.defineFunction(statement);
        return RuntimeValue.nil();
    }

    @Override
    public RuntimeValue visitReturnStatement(ReturnStatement statement) {
        RuntimeValue value = statement.hasValue()
                ? statement.getValue().accept(this)
                : RuntimeValue.nil();
        throw new ReturnSignal(value);
    }

    @Override
    public RuntimeValue visitNumberExpression(NumberExpression expression) {
        return RuntimeValue.number(expression.getValue());
    }

    @Override
    public RuntimeValue visitStringExpression(StringExpression expression) {
        return RuntimeValue.string(expression.getValue());
    }

    @Override
    public RuntimeValue visitVariableExpression(VariableExpression expression) {
        return environment.get(expression.getName());
    }

    @Override
    public RuntimeValue visitBinaryExpression(BinaryExpression expression) {
        if (expression.getOperator() == TokenType.AND) {
            RuntimeValue left = expression.getLeft().accept(this);
            if (!left.isTruthy()) return RuntimeValue.bool(false);
            return RuntimeValue.bool(expression.getRight().accept(this).isTruthy());
        }

        if (expression.getOperator() == TokenType.OR) {
            RuntimeValue left = expression.getLeft().accept(this);
            if (left.isTruthy()) return RuntimeValue.bool(true);
            return RuntimeValue.bool(expression.getRight().accept(this).isTruthy());
        }

        RuntimeValue left = expression.getLeft().accept(this);
        RuntimeValue right = expression.getRight().accept(this);

        return switch (expression.getOperator()) {
            case PLUS -> add(left, right);
            case MINUS -> RuntimeValue.number(left.asNumber() - right.asNumber());
            case STAR -> RuntimeValue.number(left.asNumber() * right.asNumber());
            case SLASH -> {
                if (right.asNumber() == 0) throw new RuntimeException("Division by zero.");
                yield RuntimeValue.number(left.asNumber() / right.asNumber());
            }
            case EQEQ -> RuntimeValue.bool(equals(left, right));
            case NEQ -> RuntimeValue.bool(!equals(left, right));
            case LT -> RuntimeValue.bool(left.asNumber() < right.asNumber());
            case GT -> RuntimeValue.bool(left.asNumber() > right.asNumber());
            case LTEQ -> RuntimeValue.bool(left.asNumber() <= right.asNumber());
            case GTEQ -> RuntimeValue.bool(left.asNumber() >= right.asNumber());
            default -> throw new RuntimeException("Unknown operator: " + expression.getOperator());
        };
    }

    private RuntimeValue add(RuntimeValue left, RuntimeValue right) {
        if (left.getType() == ValueType.STRING || right.getType() == ValueType.STRING) {
            return RuntimeValue.string(left.toString() + right.toString());
        }
        return RuntimeValue.number(left.asNumber() + right.asNumber());
    }

    private boolean equals(RuntimeValue left, RuntimeValue right) {
        if (left.getType() != right.getType()) return false;
        return switch (left.getType()) {
            case NUMBER -> left.asNumber() == right.asNumber();
            case STRING -> left.asString().equals(right.asString());
            case BOOLEAN -> left.asBoolean() == right.asBoolean();
            case NIL -> true;
            case ARRAY -> left.asArray().equals(right.asArray());
            default -> false;
        };
    }

    @Override
    public RuntimeValue visitUnaryExpression(UnaryExpression expression) {
        RuntimeValue right = expression.getRight().accept(this);
        return switch (expression.getOperator()) {
            case MINUS -> RuntimeValue.number(-right.asNumber());
            case EXCL -> RuntimeValue.bool(!right.isTruthy());
            default -> throw new RuntimeException("Unknown unary operator.");
        };
    }

    @Override
    public RuntimeValue visitAssignExpression(AssignExpression expression) {
        RuntimeValue value = expression.getValue().accept(this);
        environment.assign(expression.getName(), value);
        return value;
    }

    @Override
    public RuntimeValue visitCallExpression(CallExpression expression) {
        FunctionStatement function = environment.getFunction(expression.getName());
        List<String> params = function.getParams();

        if (params.size() != expression.getArguments().size()) {
            throw new RuntimeException("Function '" + expression.getName() + "' expects "
                    + params.size() + " arguments, got " + expression.getArguments().size() + ".");
        }

        RuntimeEnvironment previous = environment;
        environment = new RuntimeEnvironment(previous);

        try {
            for (int i = 0; i < params.size(); i++) {
                environment.define(params.get(i), expression.getArguments().get(i).accept(this));
            }

            try {
                for (Statement stmt : function.getBody()) {
                    execute(stmt);
                }
                return RuntimeValue.nil();
            } catch (ReturnSignal signal) {
                return signal.getValue();
            }
        } finally {
            environment = previous;
        }
    }

    @Override
    public RuntimeValue visitArrayExpression(ArrayExpression expression) {
        List<RuntimeValue> items = new ArrayList<>();
        for (Expression element : expression.getElements()) {
            items.add(element.accept(this));
        }
        return RuntimeValue.array(items);
    }

    @Override
    public RuntimeValue visitIndexExpression(IndexExpression expression) {
        RuntimeValue array = expression.getArray().accept(this);
        if (array.getType() != ValueType.ARRAY) {
            throw new RuntimeException("Index operator requires an array.");
        }

        RuntimeValue index = expression.getIndex().accept(this);
        if (index.getType() != ValueType.NUMBER) {
            throw new RuntimeException("Array index must be a number.");
        }

        int i = (int) index.asNumber();
        List<RuntimeValue> items = array.asArray();
        if (i < 0 || i >= items.size()) {
            throw new RuntimeException("Array index out of bounds: " + i);
        }
        return items.get(i);
    }

    @Override
    public RuntimeValue visitIndexAssignExpression(IndexAssignExpression expression) {
        RuntimeValue arrayValue = expression.getArray().accept(this);
        if (arrayValue.getType() != ValueType.ARRAY) {
            throw new RuntimeException("Index assignment requires an array.");
        }

        RuntimeValue index = expression.getIndex().accept(this);
        if (index.getType() != ValueType.NUMBER) {
            throw new RuntimeException("Array index must be a number.");
        }

        int i = (int) index.asNumber();
        List<RuntimeValue> items = arrayValue.asArray();
        if (i < 0 || i >= items.size()) {
            throw new RuntimeException("Array index out of bounds: " + i);
        }

        RuntimeValue value = expression.getValue().accept(this);
        items.set(i, value);
        return value;
    }
}
