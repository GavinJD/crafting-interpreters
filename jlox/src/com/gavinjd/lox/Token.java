package lox;

public record Token(TokenType type, String lexeme, Object literal, int line) {
    @Override
    public String toString() {
        if (literal != null) {
            return "%s %s %s".formatted(type, lexeme, literal);
        } else {
            return "%s %s".formatted(type, lexeme);
        }
    }
}
