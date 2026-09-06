package com.craftinginterpreters.lox;

class Token {
    final TokenType type; // NUMBER
    final String lexeme; // "42"
    final Object literal; // 42
    final int line; // 1

    Token(TokenType _type, String _lexeme, Object _literal, int _line) {
        this.type = _type;
        this.lexeme = _lexeme;
        this.literal = _literal;
        this.line = _line;
    }
    
    @Override
    public String toString() {
        return type + " " + lexeme + " " + literal;
    }
}
