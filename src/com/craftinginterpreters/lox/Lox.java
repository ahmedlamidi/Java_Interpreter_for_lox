package com.craftinginterpreters.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox{
    static boolean hadError = false;
    public static void main(String[] args) throws IOException {
        if (args.length >1){
            System.out.println("Usage: jlox[script]");
            System.exit(64);
        }
        else if (args.length == 1){
            runFile(args[0]);
        }
        else{
            runPrompt();
        }
    }
    public static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()), 0);
        if (hadError){
            System.exit(65);
        }
    }

    public static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);
        // This is to read the text from the command prompt of the Interactive command prompt

        int comment_depth = 0;
        for(;;){ // essentially an infinite loop
            System.out.print("> "); // print out the > to request input
            String line  = reader.readLine();
            if (line == null) break; // when it is an empty line it returns null
            comment_depth = run(line, comment_depth); // then run the line
            hadError = false;
        }
    }

    private static int run(String source, int comment_depth){
        Scanner scanner = new com.craftinginterpreters.lox.Scanner(source, comment_depth);
        List<Token> tokens = scanner.scanTokens();

        for (Token  token:tokens){
            System.out.println(token.type);
        }
        return scanner.comment_depth;
    }

    static void error(int line, String message){
        report(line, "",message);
    }

    private static void report(int line, String where, String message){
        System.err.println(
            "[Line" + line + "] " + where + ": " + message
        );
        hadError = true;
    }
}