package tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class GenerateAst {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(64);
        }
        String outputDir = args[0];

        defineAst(outputDir, "Expr", List.of(
                "Binary   : Expr left, Token operator, Expr right",
                "Grouping : Expr expression",
                "Literal  : Object value",
                "Unary    : Token operator, Expr right"
        ));
    }

    private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException {
        var path = outputDir + "/" + baseName + ".java";
        try (var writer = new PrintWriter(path, StandardCharsets.UTF_8)) {
            // output the base class
            writer.println("package lox;");
            writer.println();
            writer.println("import javax.annotation.processing.Generated;");
            writer.println("import java.util.List;");
            writer.println();
            writer.println("@Generated(\"tool.GenerateAst\")");
            writer.println("public abstract class %s {".formatted(baseName));

            defineVisitor(writer, baseName, types);
            // each subclass in base class
            for (String type : types) {
                var split = type.split(":", 2);
                var className = split[0].trim();
                var fieldList = split[1].trim();

                defineType(baseName, writer, className, fieldList);
            }

            writer.println();
            writer.println("\tpublic abstract <R> R accept(Visitor<R> visitor);");
            writer.println("}");
        }
    }

    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
        writer.println("\tpublic interface Visitor<R> {");
        for (String type : types) {
            var typeName = type.split(":")[0].trim();
            writer.println("\t\tR visit%s%s(%s %s);"
                    .formatted(typeName, baseName,
                            typeName, baseName.toLowerCase()));
        }

        writer.println("\t}");
    }

    private static void defineType(String baseName, PrintWriter writer, String className, String fieldList) {
        writer.println("\tpublic static class %s extends %s {".formatted(className, baseName));
        // constructor
        writer.println("\t\tpublic %s(%s) {".formatted(className, fieldList));
        // store parameters in fields
        var fields = fieldList.split(", ");
        for (String field : fields) {
            var name = field.split(" ")[1];
            writer.println("\t\t\tthis.%s = %s;".formatted(name, name));
        }
        writer.write("\t\t}");

        // visitor pattern
        writer.println();
        writer.println("\t\t@Override");
        writer.println("\t\tpublic <R> R accept(Visitor<R> visitor) {");
        writer.println("\t\t\treturn visitor.visit%s%s(this);".formatted(className, baseName));
        writer.println("\t\t}");

        // fields
        writer.println();
        for (String field: fields) {
            writer.println("\t\tpublic final %s;".formatted(field));
        }

        writer.println("\t}");
    }
}
