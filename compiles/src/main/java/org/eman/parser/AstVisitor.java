package org.eman.parser;

import org.eman.parser.ast.*;

public interface AstVisitor<T> {
    T visitVarStatement(VarStatement statement);
    T visitPrintStatement(PrintStatement statement);
    T visitExpressionStatement(ExpressionStatement statement);
    T visitBlockStatement(BlockStatement statement);
    T visitIfStatement(IfStatement statement);
    T visitWhileStatement(WhileStatement statement);
    T visitFunctionStatement(FunctionStatement statement);
    T visitReturnStatement(ReturnStatement statement);

    T visitNumberExpression(NumberExpression expression);
    T visitStringExpression(StringExpression expression);
    T visitVariableExpression(VariableExpression expression);
    T visitBinaryExpression(BinaryExpression expression);
    T visitUnaryExpression(UnaryExpression expression);
    T visitAssignExpression(AssignExpression expression);
    T visitCallExpression(CallExpression expression);
    T visitArrayExpression(ArrayExpression expression);
    T visitIndexExpression(IndexExpression expression);
    T visitIndexAssignExpression(IndexAssignExpression expression);
}
