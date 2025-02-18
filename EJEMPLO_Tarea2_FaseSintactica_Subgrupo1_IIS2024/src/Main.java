import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Token {
    public String type;
    public String value;

    public Token(String type, String value) {
        this.type = type;
        this.value = value;
    }
}

class Lexer {
    private static final String[][] TOKEN_SPECIFICATION = {
            {"NUMBER",   "\\d+"},       // Integer number
            {"PLUS",     "\\+"},        // Addition
            {"MINUS",    "-"},          // Subtraction
            {"TIMES",    "\\*"},        // Multiplication
            {"DIVIDE",   "/"},          // Division
            {"LPAREN",   "\\("},        // Left Parenthesis
            {"RPAREN",   "\\)"},        // Right Parenthesis
            {"SKIP",     "[ \\t]+"},    // Skip spaces and tabs
            {"MISMATCH", "."}           // Any other character
    };

    private static final Pattern TOKEN_PATTERN;

    static {
        StringBuilder tokenPatternBuilder = new StringBuilder();
        for (String[] spec : TOKEN_SPECIFICATION) {
            tokenPatternBuilder.append(String.format("|(?<%s>%s)", spec[0], spec[1]));
        }
        TOKEN_PATTERN = Pattern.compile(tokenPatternBuilder.substring(1));
    }

    public static List<Token> tokenize(String code) throws SyntaxError {
        List<Token> tokens = new ArrayList<>();
        Matcher matcher = TOKEN_PATTERN.matcher(code);

        while (matcher.find()) {
            for (String tokenType : TOKEN_SPECIFICATION[0]) {
                if (matcher.group(tokenType) != null) {
                    if (tokenType.equals("NUMBER")) {
                        tokens.add(new Token("NUMBER", matcher.group()));
                    } else if (!tokenType.equals("SKIP")) {
                        tokens.add(new Token(tokenType, matcher.group()));
                    }
                    break;
                }
            }
        }
        return tokens;
    }
}

class Parser {
    private List<Token> tokens;
    private int pos = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private Token currentToken() {
        return pos < tokens.size() ? tokens.get(pos) : new Token("EOF", "");
    }

    private void match(String expectedType) {
        if (currentToken().type.equals(expectedType)) {
            pos++;
        } else {
            errorRecovery(expectedType);
        }
    }

    private void errorRecovery(String expectedType) {
        System.out.printf("Syntax error: expected %s, but found %s\n", expectedType, currentToken().type);
        while (!currentToken().type.equals(expectedType) && !currentToken().type.equals("EOF")) {
            pos++;
        }
    }

    public void parse() {
        expr();
    }

    private void expr() {
        term();
        while (currentToken().type.equals("PLUS") || currentToken().type.equals("MINUS")) {
            match(currentToken().type);
            term();
        }
    }

    private void term() {
        factor();
        while (currentToken().type.equals("TIMES") || currentToken().type.equals("DIVIDE")) {
            match(currentToken().type);
            factor();
        }
    }

    private void factor() {
        if (currentToken().type.equals("NUMBER")) {
            match("NUMBER");
        } else if (currentToken().type.equals("LPAREN")) {
            match("LPAREN");
            expr();
            match("RPAREN");
        } else {
            errorRecovery("NUMBER or LPAREN");
        }
    }
}

public class Main {
    public static void main(String[] args) {
        String code = "3 + * 5 * ( 10 - 2)";  // Input with a syntax error
        try {
            List<Token> tokens = Lexer.tokenize(code);
            Parser parser = new Parser(tokens);
            parser.parse();
        } catch (SyntaxError e) {
            System.out.println(e.getMessage());
        }
    }
}

class SyntaxError extends Exception {
    public SyntaxError(String message) {
        super(message);
    }
}
