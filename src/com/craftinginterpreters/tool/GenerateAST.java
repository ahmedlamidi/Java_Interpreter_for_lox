package com.craftinginterpreters.tool;

import com.sun.java.accessibility.util.EventID;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class GenerateAST {
    public static void main(String[] args) throws IOException{
        if(args.length != 1){
            System.err.println("Usage: generate_ast <output directory>");// throw error if more than one arg
            System.exit(64);
        }
        String outputDir = args[0];
        defineAst(outputDir, "Expr", Arrays.asList(
                "Binary   : Expr left, Token operator, Expr right",
                "Grouping : Expr expression",
                "Literal  : Object value",
                "Unary    : Token operator, Expr right"
        ));
    }
    private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException{
        String path = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, StandardCharsets.UTF_8); // create a writer for the file specified
        writer.println("package com.craftinginterpreters.lox;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("abstract class " + baseName + " {");
        defineVistitor(writer, baseName, types);
        for (String type: types){
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer, baseName, className, fields);

        }
        writer.println();
        writer.println("  abstract <R> R accept(Visitor<R> visitor);");
        // this method will be overwritten by each subclass to return the right value when a function is called
        writer.println("}");
        writer.close();

    }
    private static void defineVistitor(
            PrintWriter writer, String baseName, List<String> types
    ){
        writer.println("  interface Visitor<R> {");
        for (String type: types){
            String typeName = type.split(":")[0].trim();
            writer.println("    R visit" + typeName + baseName + "(" +
                    typeName + " " + baseName.toLowerCase() + ")");
        }

        writer.println("    }");
        // iterate through all the classes and create a visitor for each one
    }
    private static void defineType(
            PrintWriter writer,  String baseName,
            String className, String fieldList){
        writer.println(" static class " + className + " extends " + baseName + " {");
        writer.println("    " + className + "(" + fieldList + ") {");

        // storing the parameters that we have in fields

        String[] fields = fieldList.split(", ");
        for (String field : fields){
            String name = field.split(" ")[1];
            writer.println("    this." + name + " = " + name + ";");
        }

        writer.println("    }");
        writer.println();
        writer.println("    @override");
        writer.println("   <R> R accept(Visitor<R> visitor {");
        writer.println("    return visitor.visit" + className + baseName + "(this);");


        for (String field : fields){
            writer.println("    final "  + field  + ";");
        }
        writer.println("  }");
    }

}
