package com.craftinginterpreters.lox;

public class Token {
    final TokenType type;
    final String Lexeme;
    final Object literal;
    final int line;

    Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.Lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    public String tostring(){
        return type + " " + Lexeme + " " + literal;
    }
}
