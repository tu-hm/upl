package org.upl;

import org.upl.graph.ASTGraph;
import org.upl.lexer.ILexer;
import org.upl.lexer.JFlexLexer;
import org.upl.lexer.Lexer;
import org.upl.lexer.Token;
import org.upl.parser.Parser;
import org.upl.parser.manual.BottomUp;
import org.upl.parser.manual.TopDown;

import java.io.*;
import java.util.List;

public class Main {
    private static String filePath = null;
    public static String getFilePath() {
        return filePath;
    }

    static boolean hasCompileError = false;

    public static void error(int line, int column, String message) {
        System.err.printf("Error: %s at %d:%d\n", message, line, column);
    }
    public static void error(int line, String where, String message) {
        System.err.printf("[line %d] Error %s : %s\n", line, where, message);
    }
    public static void error(int line, int column, String where, String message) {
        System.err.printf("[%d:%d] Error %s : %s\n", line, column, where, message);
    }
    public static void compileError(Token token, String message) {
        error(token.getLine(), token.getColumn(), String.format("at '%s'", token.getLexeme()), message);
        hasCompileError = true;
    }
    public static void compileError(int line, int column, String message) {
        error(line, column, message);
        hasCompileError = true;
    }


    public static void main(String[] args) throws FileNotFoundException {
//        String executionPath = Paths.get("").toAbsolutePath().toString();
//        System.out.println("Program executed from: " + executionPath);
        boolean useJflex = false;
        for (String arg : args) {
            if (arg.startsWith("--")) {
                String part = arg.substring(2);
                if (part.equals("jflex")) {
                    useJflex = true;
                }
                continue;
            } else {
                filePath = arg;
            }
        }

        if (filePath == null) {
            System.err.println("Missing input file");
            System.exit(1);
        }

        ILexer lexer;
        Reader reader = new InputStreamReader(new FileInputStream(filePath));
        if (useJflex) {
            lexer = new JFlexLexer(reader);
        } else {
            StringBuilder content = new StringBuilder();
            try (BufferedReader br = new BufferedReader(reader)) {
                String line;
                while ((line = br.readLine()) != null) {
                    content.append(line).append("\n"); // Append the line to the content
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            lexer = new Lexer(content.toString());
        }

        List<Token> tokens = lexer.scanTokens();
        for (Token token : tokens) {
            System.out.println("Token: " + token.getType() + " at line " + token.getLine() + " column " + token.getColumn());
        }

        Parser parser = new TopDown(
                new InputStreamReader(new FileInputStream("CFG.txt")));

        ASTGraph parsed = parser.parse(tokens);
        if (!hasCompileError) {
            parsed.drawSyntaxTree();
        }
    }
}