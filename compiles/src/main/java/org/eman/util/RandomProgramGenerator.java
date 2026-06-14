package org.eman.util;

import java.util.*;

public class RandomProgramGenerator {
    private final Random random = new Random();

    private final String[] varNames = { "x", "y", "z", "alpha", "beta", "count", "total", "index", "sum" };

    private final List<String> declaredVars = new ArrayList<>();

    private final String[] mathOps = { "+", "-", "*", "/" };
    private final String[] compareOps = { "==", "!=", "<", ">", "<=", ">=" };
    private final String[] logicOps = { "&&", "||" };

    public String generate(int statementCount, long seed) {
        random.setSeed(seed);
        return generate(statementCount);
    }

    public String generate() {
        return generate(10);
    }

    public String generate(int statementCount) {
        declaredVars.clear();
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < 3; i++) {
            builder.append(generateVarDeclaration(0)).append("\n");
        }

        generateBlock(builder, statementCount, 0);

        return builder.toString();
    }

    private void generateBlock(StringBuilder builder, int count, int indentLevel) {
        String indent = "    ".repeat(Math.max(0, indentLevel));

        for (int i = 0; i < count; i++) {

            int statementType = random.nextInt(5);

            if (indentLevel > 2 && statementType > 2) {
                statementType = random.nextInt(3);
            }

            switch (statementType) {
                case 0:
                    builder.append(generateVarDeclaration(indentLevel)).append("\n");
                    break;
                case 1:
                    if (!declaredVars.isEmpty()) {
                        builder.append(indent)
                                .append(getRandomVar())
                                .append(" = ")
                                .append(generateExpression())
                                .append(";\n");
                    } else {
                        builder.append(generateVarDeclaration(indentLevel)).append("\n"); 
                    }
                    break;
                case 2:
                    builder.append(indent)
                            .append("print ")
                            .append(generateExpression())
                            .append(";\n");
                    break;
                case 3:
                    builder.append(indent)
                            .append("if (")
                            .append(generateCondition())
                            .append(") {\n");
                    generateBlock(builder, random.nextInt(1, 4), indentLevel + 1);

                    if (random.nextDouble() > 0.5) { 
                        builder.append(indent).append("} else {\n");
                        generateBlock(builder, random.nextInt(1, 3), indentLevel + 1);
                    }
                    builder.append(indent).append("}\n");
                    break;
                case 4:
                    builder.append(indent)
                            .append("while (")
                            .append(generateCondition())
                            .append(") {\n");
                    generateBlock(builder, random.nextInt(1, 4), indentLevel + 1);
                    builder.append(indent).append("}\n");
                    break;
            }
        }
    }

    private String generateVarDeclaration(int indentLevel) {
        String indent = "    ".repeat(Math.max(0, indentLevel));

        String varName = varNames[random.nextInt(varNames.length)];
        if (!declaredVars.contains(varName)) {
            declaredVars.add(varName);
        }

        return indent + "var " + varName + " = " + generateExpression() + ";";
    }

    private String generateExpression() {
        
        if (random.nextDouble() > 0.6 || declaredVars.isEmpty()) {
            return String.valueOf(random.nextInt(1, 100));
        }

        if (random.nextDouble() > 0.5) {
            return getRandomVar();
        }

        String left = random.nextDouble() > 0.5 ? getRandomVar() : String.valueOf(random.nextInt(1, 100));
        String right = random.nextDouble() > 0.5 ? getRandomVar() : String.valueOf(random.nextInt(1, 100));
        String op = mathOps[random.nextInt(mathOps.length)];

        return left + " " + op + " " + right;
    }

    private String generateCondition() {
        
        String left = getRandomVarOrNumber();
        String right = getRandomVarOrNumber();
        String compOp = compareOps[random.nextInt(compareOps.length)];

        String condition = left + " " + compOp + " " + right;

        if (random.nextDouble() > 0.7) {
            String logicOp = logicOps[random.nextInt(logicOps.length)];
            String extraLeft = getRandomVarOrNumber();
            String extraRight = getRandomVarOrNumber();
            String extraComp = compareOps[random.nextInt(compareOps.length)];

            condition = "(" + condition + ") " + logicOp + " (" + extraLeft + " " + extraComp + " " + extraRight + ")";
        }

        return condition;
    }

    private String getRandomVar() {
        if (declaredVars.isEmpty()) return "1"; 
        return declaredVars.get(random.nextInt(declaredVars.size()));
    }

    private String getRandomVarOrNumber() {
        if (!declaredVars.isEmpty() && random.nextDouble() > 0.5) {
            return getRandomVar();
        }
        return String.valueOf(random.nextInt(1, 100));
    }

    public void generateAndPrint(int statementCount) {
        System.out.println(generate(statementCount));
    }

    public void generateAndPrint(int statementCount, long seed) {
        System.out.println(generate(statementCount, seed));
    }
}
