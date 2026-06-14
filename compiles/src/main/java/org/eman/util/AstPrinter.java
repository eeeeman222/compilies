package org.eman.util;

import org.eman.parser.ast.*;
import org.eman.parser.ast.Statement;

import java.util.List;

public class AstPrinter {

    public void print(List<Statement> statements) {
        System.out.println("Root (Program)");
        for (int i = 0; i < statements.size(); i++) {
            printNode(statements.get(i), "", i == statements.size() - 1);
        }
    }

    private void printNode(Object node, String indent, boolean isLast) {
        if (node == null) return;

        String marker = isLast ? "└── " : "├── ";
        System.out.print(indent + marker);
        String childIndent = indent + (isLast ? "    " : "│   ");

        if (node instanceof VarStatement v) {
            System.out.println("VarStatement: " + v.getName());
            if (v.getInitializer() != null) printNode(v.getInitializer(), childIndent, true);
        } else if (node instanceof PrintStatement p) {
            System.out.println("PrintStatement");
            printNode(p.getExpression(), childIndent, true);
        } else if (node instanceof IfStatement i) {
            System.out.println("IfStatement");
            printNode(i.getCondition(), childIndent, false);
            printNode(i.getThenBranch(), childIndent, i.getElseBranch() == null);
            if (i.getElseBranch() != null) printNode(i.getElseBranch(), childIndent, true);
        } else if (node instanceof WhileStatement w) {
            System.out.println("WhileStatement");
            printNode(w.getCondition(), childIndent, false);
            printNode(w.getBody(), childIndent, true);
        } else if (node instanceof BlockStatement b) {
            System.out.println("BlockStatement");
            List<Statement> statements = b.getStatements();
            for (int j = 0; j < statements.size(); j++) {
                printNode(statements.get(j), childIndent, j == statements.size() - 1);
            }
        } else if (node instanceof ExpressionStatement e) {
            System.out.println("ExpressionStatement");
            printNode(e.getExpression(), childIndent, true);
        } else if (node instanceof FunctionStatement f) {
            System.out.println("FunctionStatement: " + f.getName() + f.getParams());
            List<Statement> body = f.getBody();
            for (int j = 0; j < body.size(); j++) {
                printNode(body.get(j), childIndent, j == body.size() - 1);
            }
        } else if (node instanceof ReturnStatement r) {
            System.out.println("ReturnStatement");
            if (r.hasValue()) printNode(r.getValue(), childIndent, true);
        } else if (node instanceof BinaryExpression bin) {
            System.out.println("BinaryExpression: " + bin.getOperator());
            printNode(bin.getLeft(), childIndent, false);
            printNode(bin.getRight(), childIndent, true);
        } else if (node instanceof UnaryExpression un) {
            System.out.println("UnaryExpression: " + un.getOperator());
            printNode(un.getRight(), childIndent, true);
        } else if (node instanceof AssignExpression assign) {
            System.out.println("AssignExpression: " + assign.getName());
            printNode(assign.getValue(), childIndent, true);
        } else if (node instanceof CallExpression call) {
            System.out.println("CallExpression: " + call.getName());
            List<Expression> args = call.getArguments();
            for (int j = 0; j < args.size(); j++) {
                printNode(args.get(j), childIndent, j == args.size() - 1);
            }
        } else if (node instanceof ArrayExpression arr) {
            System.out.println("ArrayExpression");
            List<Expression> elements = arr.getElements();
            for (int j = 0; j < elements.size(); j++) {
                printNode(elements.get(j), childIndent, j == elements.size() - 1);
            }
        } else if (node instanceof IndexExpression idx) {
            System.out.println("IndexExpression");
            printNode(idx.getArray(), childIndent, false);
            printNode(idx.getIndex(), childIndent, true);
        } else if (node instanceof IndexAssignExpression idxAssign) {
            System.out.println("IndexAssignExpression");
            printNode(idxAssign.getArray(), childIndent, false);
            printNode(idxAssign.getIndex(), childIndent, false);
            printNode(idxAssign.getValue(), childIndent, true);
        } else if (node instanceof NumberExpression num) {
            System.out.println("Number: " + num.getValue());
        } else if (node instanceof StringExpression str) {
            System.out.println("String: \"" + str.getValue() + "\"");
        } else if (node instanceof VariableExpression varExpr) {
            System.out.println("Variable: " + varExpr.getName());
        } else {
            System.out.println("Unknown Node: " + node.getClass().getSimpleName());
        }
    }
}
