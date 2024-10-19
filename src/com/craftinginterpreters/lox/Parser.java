package com.craftinginterpreters.lox;

import java.util.List;
import static com.craftinginterpreters.lox.TokenType.*;

class Parser {
    private final List<Token> tokens; // these are the tokens we are going to parse
    private int current = 0;
    Parser(List<Token> tokens){
        this.tokens = tokens; // initialize with a list of tokens
    }

    private Expr expression(){
        // grammar is expression -> equality
        return equality(); // an expression is equivalent to an equality right now
    }

    private Expr equality(){
        // grammar is equality -> comparison ( ("!=" | "==") comparison) *;
        Expr expr = comparison();
        while(match(BANG_EQUAL, BANG)){
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }
        // this is left associative equality since it would evaluate left side first
        return expr;
    }

    private Expr comparison(){
        // grammar is term (( ">" | ">=" | "<" | "<=" ) term )*;
        Expr expr = term();
        while(match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)){
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right); // it is in binary form
        }
        return expr;
    }

    private Expr term(){
        // grammar is factor (("+" | "-") factor)*;
        Expr expr = factor();
        while(match(PLUS, MINUS)){
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr factor(){
        // grammar is unary(("/" | "*") unary)*;
        Expr expr = unary();
        while(match(SLASH, STAR)){
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr unary(){
        // grammar is ("!" | "-") unary | primary
        if (match(BANG, MINUS)){
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        return primary();
    }

    private Expr primary(){
        // grammar is Number | String | "true" | "false" | "nil" | ( expression )
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);

        if (match(STRING, NUMBER)){
            return new Expr.Literal(previous().literal);
        }
        if (match(LEFT_PAREN)){
            Expr expr = expression();
            // we need to match the end of the expression with a right parentheses
            consume(RIGHT_PAREN, "expected right parentheses");
            // to do add a function to check for a type and return an error message
            return new Expr.Grouping(expr);
        }
        return null;
    }

    private boolean match(TokenType... types){
        // function to match the current token to any in the list of types
        for (TokenType type : types){
            if (check(type)){
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type){
        // if we are end it is automatically false , return if the current type is the correct type
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private Token advance(){
        if(!isAtEnd()) current ++;
        return previous();
    }

    private boolean isAtEnd(){
        return peek().type == EOF;
    }

    private Token peek(){
        return tokens.get(current);
    }

    private Token previous(){
        return tokens.get(current - 1);
    }

}
