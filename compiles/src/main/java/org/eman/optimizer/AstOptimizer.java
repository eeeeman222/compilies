package org.eman.optimizer;

import org.eman.parser.AstVisitor;
import org.eman.parser.ast.*;
import org.eman.lexer.TokenType;

import java.util.ArrayList;
import java.util.List;

public class AstOptimizer implements AstVisitor<Object> {
    public List<Statement> optimize(List<Statement> statements) {
        List<Statement> result = new ArrayList<>();
        for (Statement statement : statements) {
            Statement optimized = (Statement) statement.accept(this);
            if (optimized != null) {
                result.add(optimized);
            }
        }
        return result;
    }

    @Override
    public Object visitVarStatement(VarStatement statement) {
        Expression initializer = statement.hasInitializer()
                ? (Expression) statement.getInitializer().accept(this)
                : null;
        return new VarStatement(statement.getName(), initializer);
    }

    @Override
    public Object visitPrintStatement(PrintStatement statement) {
        return new PrintStatement((Expression) statement.getExpression().accept(this));
    }

    @Override
    public Object visitExpressionStatement(ExpressionStatement statement) {
        return new ExpressionStatement((Expression) statement.getExpression().accept(this));
    }

    @Override
    public Object visitBlockStatement(BlockStatement statement) {
        List<Statement> optimized = new ArrayList<>();
        for (Statement inner : statement.getStatements()) {
            Statement stmt = (Statement) inner.accept(this);
            if (stmt != null) optimized.add(stmt);
        }
        return new BlockStatement(optimized);
    }

    @Override
    public Object visitIfStatement(IfStatement statement) {
        Expression condition = (Expression) statement.getCondition().accept(this);

        if (condition instanceof NumberExpression num) {
            if (num.getValue() != 0) {
                return statement.getThenBranch().accept(this);
            }
            return statement.getElseBranch() != null
                    ? statement.getElseBranch().accept(this)
                    : null;
        }

        return new IfStatement(
                condition,
                (Statement) statement.getThenBranch().accept(this),
                statement.getElseBranch() != null
                        ? (Statement) statement.getElseBranch().accept(this)
                        : null
        );
    }

    @Override
    public Object visitWhileStatement(WhileStatement statement) {
        Expression condition = (Expression) statement.getCondition().accept(this);

        if (condition instanceof NumberExpression num && num.getValue() == 0) {
            return null;
        }

        return new WhileStatement(
                condition,
                (Statement) statement.getBody().accept(this)
        );
    }

    @Override
    public Object visitFunctionStatement(FunctionStatement statement) {
        List<Statement> body = new ArrayList<>();
        for (Statement inner : statement.getBody()) {
            Statement stmt = (Statement) inner.accept(this);
            if (stmt != null) body.add(stmt);
        }
        return new FunctionStatement(statement.getName(), statement.getParams(), body);
    }

    @Override
    public Object visitReturnStatement(ReturnStatement statement) {
        Expression value = statement.hasValue()
                ? (Expression) statement.getValue().accept(this)
                : null;
        return new ReturnStatement(value);
    }

    @Override
    public Object visitNumberExpression(NumberExpression expression) {
        return expression;
    }

    @Override
    public Object visitStringExpression(StringExpression expression) {
        return expression;
    }

    @Override
    public Object visitVariableExpression(VariableExpression expression) {
        return expression;
    }

    @Override
    public Object visitBinaryExpression(BinaryExpression expression) {
        Expression left = (Expression) expression.getLeft().accept(this);
        Expression right = (Expression) expression.getRight().accept(this);

        if (left instanceof NumberExpression ln && right instanceof NumberExpression rn) {
            Double folded = foldNumbers(expression.getOperator(), ln.getValue(), rn.getValue());
            if (folded != null) return new NumberExpression(folded);
        }

        if (expression.getOperator() == TokenType.PLUS
                && left instanceof StringExpression ls
                && right instanceof StringExpression rs) {
            return new StringExpression(ls.getValue() + rs.getValue());
        }

        return new BinaryExpression(left, expression.getOperator(), right);
    }

    private Double foldNumbers(TokenType op, double left, double right) {
        return switch (op) {
            case PLUS -> left + right;
            case MINUS -> left - right;
            case STAR -> left * right;
            case SLASH -> right != 0 ? left / right : null;
            default -> null;
        };
    }

    @Override
    public Object visitUnaryExpression(UnaryExpression expression) {
        Expression right = (Expression) expression.getRight().accept(this);

        if (expression.getOperator() == TokenType.MINUS && right instanceof NumberExpression num) {
            return new NumberExpression(-num.getValue());
        }

        return new UnaryExpression(expression.getOperator(), right);
    }

    @Override
    public Object visitAssignExpression(AssignExpression expression) {
        return new AssignExpression(expression.getName(), (Expression) expression.getValue().accept(this));
    }

    @Override
    public Object visitCallExpression(CallExpression expression) {
        List<Expression> args = new ArrayList<>();
        for (Expression arg : expression.getArguments()) {
            args.add((Expression) arg.accept(this));
        }
        return new CallExpression(expression.getName(), args);
    }

    @Override
    public Object visitArrayExpression(ArrayExpression expression) {
        List<Expression> elements = new ArrayList<>();
        for (Expression element : expression.getElements()) {
            elements.add((Expression) element.accept(this));
        }
        return new ArrayExpression(elements);
    }

    @Override
    public Object visitIndexExpression(IndexExpression expression) {
        return new IndexExpression(
                (Expression) expression.getArray().accept(this),
                (Expression) expression.getIndex().accept(this)
        );
    }

    @Override
    public Object visitIndexAssignExpression(IndexAssignExpression expression) {
        return new IndexAssignExpression(
                (Expression) expression.getArray().accept(this),
                (Expression) expression.getIndex().accept(this),
                (Expression) expression.getValue().accept(this)
        );
    }
}
