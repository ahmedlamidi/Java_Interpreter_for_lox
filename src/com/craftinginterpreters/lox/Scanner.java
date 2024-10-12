package com.craftinginterpreters.lox;
import javax.crypto.NullCipher;
import javax.lang.model.type.NullType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.craftinginterpreters.lox.TokenType.*;


class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;
    public int comment_depth;

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("class", CLASS);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("for", FOR);
        keywords.put("fun", FUN);
        keywords.put("if", IF);
        keywords.put("nil", NIL);
        keywords.put("or", OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("super", SUPER);
        keywords.put("this", THIS);
        keywords.put("true", TRUE);
        keywords.put("var", VAR);
        keywords.put("while", WHILE);
    }
    Scanner(String source, int comment_depth) {
        this.source = source;
        this.comment_depth = comment_depth;
    }

    private boolean isAtend(){
        return current >= source.length(); // we see if we are past the last character in the source being considered
    }

    List<Token> scanTokens(){
        while(!isAtend()){
            start = current;
            scanToken();
        }
        return tokens;
    }

    private void scanToken() {
        System.out.println(comment_depth);
        System.out.println(line);
        if (comment_depth != 0) {
            multiline_comment();
        } else {
            char c = advance();
            switch (c) {
                case '(':
                    addToken(LEFT_PAREN);
                    break;
                case ')':
                    addToken(RIGHT_PAREN);
                    break;
                case '{':
                    addToken(LEFT_BRACE);
                    break;
                case '}':
                    addToken(RIGHT_BRACE);
                    break;
                case ',':
                    addToken(COMMA);
                    break;
                case '.':
                    addToken(DOT);
                    break;
                case '-':
                    addToken(MINUS);
                    break;
                case '+':
                    addToken(PLUS);
                    break;
                case '*':
                    addToken(STAR);
                    break;
                case ';':
                    addToken(SEMICOLON);
                    break;
                case '!':
                    addToken(match('=') ? BANG_EQUAL : BANG);
                    break;
                case '<':
                    addToken(match('=') ? LESS_EQUAL : LESS);
                    break;
                case '=':
                    addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                    break;
                case '>':
                    addToken(match('=') ? GREATER_EQUAL : GREATER);
                    break;
                case '/':
                    if (match('/')) {
                        while (peek() != '\n' && !isAtend()) advance();
                    } else if (match('*')) {
                        comment_depth += 1;
                        multiline_comment();
                    } else {
                        addToken(SLASH);
                    }
                    break;
                // if we get to a / we want to check until a new line or the end of the file
                case ' ':
                    break;
                case '\t':
                    break;
                case '\r':
                    break;
                case '\n':
                    line++;
                    break;
                case '"':
                    string();
                    break;
                default:
                    if (isDigit(c)) {
                        number(); // if we see the first digit is a number then we call the number function
                    } else if (isAlpha(c)) {
                        identifier(); // we assume it is an identifier if it does not start with a number and is an alpha
                    } else {
                        Lox.error(line, "unexpected character");
                    }
                    break;

                // Attempt to get all token types based on the character that we see in the current line

            }
        }
    }
    private void addToken(TokenType type){
        addToken(type, null);
    }

    private boolean isDigit(char c){
        return (c >= '0' && c <= '9');
    }


    void multiline_comment(){
        while (comment_depth != 0){
            if (peek() == '/' && peeknext() == '*'){
                comment_depth += 1;
                advance();
                advance();
            } else if (peek() == '*'  && peeknext() == '/' ) {
                comment_depth -= 1;
                advance();
                advance();
            } else if (isAtend()) {
                System.out.println("at end");
                break;
            } else{
                advance();
            }
        }
    }

    private void number(){
        while(isDigit(peek())) advance();
        //take the numbers before the decimal point

        if (peek() == '.' && isDigit(peeknext())){
            advance(); // take in the . from the number
        }

        while(isDigit(peek())) advance();
        // take the numbers after the decimal point

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
        // we add a double of the elements that we have gone over from the start to now
    }

    private boolean isAlpha(char c){
        return (c >= 'a' && c <='z') || (c >= 'A' && c <= 'Z') || c == '_';
        //used to check if we see an alphabet or an underscore
    }

    private boolean isAlphanumeric(char c){
        return isDigit(c) || isAlpha(c);
    }

    private void identifier(){
        while (isAlphanumeric(peek())) advance();
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER;
        addToken(type);

    }

    private void addToken(TokenType type, Object literal){
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));

    }

    private char advance(){
        return source.charAt(current++);
    }

    private void string() {
        while (peek() != '"' && !isAtend()){
            if (peek() == '\n') line++;
            advance(); // if we get a new line we want to change to the next line in the counter
        }

        if (isAtend()){
            Lox.error(line, "unterminated string");
            return ; // if the string does not finish then return an error saying we got to the end
        }

        advance(); // we get the last " out of the string

        String value = source.substring(start + 1, current - 1); // we remove the surrounding " characters
        addToken(STRING, value); // add a token of type string with a value of the value
    }
    private boolean match(char expected){
        if (isAtend()) return false;
        if (source.charAt(current) != expected) return false;
        // if the character is what we expect then we consume the next character also
        current++;
        return true;
    }

    private char peek(){
        if (isAtend()) return '\0';
        return source.charAt(current); // peek is used to check the next char without consuming it
    }

    private char peeknext(){
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1); // this is used to peek the char after the next
    }
}
