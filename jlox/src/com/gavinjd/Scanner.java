import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    public Scanner(String source) {
        this.source = source;
    }

    private int start = 0;
    private int current = 0;
    private int line = 1;

    private static final Map<String, TokenType> RESERVED_KEYWORDS;

    static {
        RESERVED_KEYWORDS = new HashMap<>();
        RESERVED_KEYWORDS.put("and", TokenType.AND);
        RESERVED_KEYWORDS.put("class", TokenType.CLASS);
        RESERVED_KEYWORDS.put("else", TokenType.ELSE);
        RESERVED_KEYWORDS.put("false", TokenType.FALSE);
        RESERVED_KEYWORDS.put("fun", TokenType.FUN);
        RESERVED_KEYWORDS.put("for", TokenType.FOR);
        RESERVED_KEYWORDS.put("if", TokenType.IF);
        RESERVED_KEYWORDS.put("nul", TokenType.NUL);
        RESERVED_KEYWORDS.put("or", TokenType.OR);
        RESERVED_KEYWORDS.put("print", TokenType.PRINT);
        RESERVED_KEYWORDS.put("return", TokenType.RETURN);
        RESERVED_KEYWORDS.put("super", TokenType.SUPER);
        RESERVED_KEYWORDS.put("this", TokenType.THIS);
        RESERVED_KEYWORDS.put("true", TokenType.TRUE);
        RESERVED_KEYWORDS.put("var", TokenType.VAR);
        RESERVED_KEYWORDS.put("while", TokenType.WHILE);
    }

    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(' -> addToken(TokenType.LEFT_PAREN);
            case ')' -> addToken(TokenType.RIGHT_PAREN);
            case '{' -> addToken(TokenType.LEFT_BRACE);
            case '}' -> addToken(TokenType.RIGHT_BRACE);
            case ',' -> addToken(TokenType.COMMA);
            case '.' -> addToken(TokenType.DOT);
            case '-' -> addToken(TokenType.MINUS);
            case '+' -> addToken(TokenType.PLUS);
            case ';' -> addToken(TokenType.SEMICOLON);
            case '*' -> addToken(TokenType.STAR);
            case '!' -> addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
            case '=' -> addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
            case '>' -> addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
            case '<' -> addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
            case '/' -> {
                if (match('/')) {
                    // handle comments
                    while (peek() != '\n' && !isAtEnd()) {
                        advance();
                    }
                    line++; // note: this line isn't in the book, so
                    // technically the book assumes commented
                    // lines don't exist?
                } else {
                    addToken(TokenType.SLASH);
                }
            }
            case '"' -> string();
            // note: in the book, they instead of enumerating all digits just
            //       do a 'isDigit' check in the default case branch
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> number();

            case ' ', '\r', '\t' -> {
            } // ignore whitespace
            case '\n' -> line++;
            default -> {
                if (isAlpha(peek())) {
                    identifierOrReservedKeyword();
                } else {
                    Lox.error(line, "Unexpected character '%s'".formatted(peek()));
                }
            }
        }
    }

    private void identifierOrReservedKeyword() {
        // identify by longest match/maximal munch
        while (isAlphaNumeric(peek())) {
            advance();
        }

        String value = source.substring(start, current);
        if (RESERVED_KEYWORDS.containsKey(value)) {
            addToken(RESERVED_KEYWORDS.get(value));
        } else {
            addToken(TokenType.IDENTIFIER, value);
        }
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z')
                || (c >= 'A' && c <= 'Z')
                || c == '_';
    }

    private void number() {
        while (isDigit(peek())) {
            advance();
        }

        // parsing fractional parts
        if (peek() == '.' && isDigit(peekNext())) {
            advance();

            while (isDigit(peek())) {
                advance();
            }
        }

        Double value = Double.parseDouble(source.substring(start, current));
        addToken(TokenType.NUMBER, value);
    }

    /**
     * Excerpt from book:
     * <blockquote>
     * I could have made {@code peek()} take a parameter for
     * the number of characters ahead to look instead of defining two
     * functions, but that would allow arbitrarily far lookahead. Providing
     * these two functions makes it clearer to a reader of the code that
     * our scanner looks ahead at most two characters.
     * </blockquote>
     */
    private char peekNext() {
        return source.charAt(current + 1);
    }

    /**
     * Created because {@link Character#isDigit(char)} allows digits of
     * different languages, when we only want ascii digits
     */
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            // todo: how do you prohibit multi-line strings?
            if (peek() == '\n') { // allow multi-line string
                line++;
            }
            advance();
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated string");
            return;
        }

        advance(); // closing "

        // todo: here is where all the string escaping should go
        String value = source.substring(start + 1, current - 1);
        addToken(TokenType.STRING, value);
    }

    private char peek() {
        if (isAtEnd()) {
            return '\0';
        }
        return source.charAt(current);
    }

    /**
     * A conditional advance, only consume the next character if it matches
     */
    private boolean match(char expected) {
        if (isAtEnd()) {
            return false;
        }
        if (peek() != expected) {
            return false;
        }

        advance();
        return true;
    }

    private void addToken(TokenType tokenType) {
        addToken(tokenType, null);
    }

    private void addToken(TokenType tokenType, Object value) {
        String text = source.substring(start, current);
        tokens.add(new Token(tokenType, text, value, line));
    }

    private char advance() {
        return source.charAt(current++);
    }


    private boolean isAtEnd() {
        return current >= source.length();
    }

}
