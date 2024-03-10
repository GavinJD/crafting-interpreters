import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
    private static boolean hadError = false;
    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }


    private static void runPrompt() throws IOException {
        var input = new InputStreamReader(System.in);
        var reader = new BufferedReader(input);

        while(true) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null || line.equals("exit")) break;
            run(line);

            hadError = false; // reset error before next line
        }
    }

    private static void runFile(String fileName) throws IOException {
        var fileData = Files.readString(Paths.get(fileName));
        run(fileData);

        if (hadError) {
            System.exit(65);
        }
    }

    private static void run(String source) {
        var scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        for (var token: tokens) {
            System.out.println(token);
        }
    }

    public static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String where, String message) {
        System.err.printf("[line %d] Error %s:%s%n", line, where, message);
        hadError = true;
    }
}