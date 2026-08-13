package it.uniroma2.ispw.ciboamico.tools;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

// Reverse-engineering ausiliario: legge le classi Java reali del

public final class GeneraClassDiagram {

    private static final Path SRC = Path.of("src", "main", "java");
    private static final Path OUT = Path.of("target", "uml");

    private GeneraClassDiagram() { }

    public static void main(String[] args) throws IOException {
        Map<String, String> stereotipi = new LinkedHashMap<>();
        Map<String, List<String>> relazioni = new LinkedHashMap<>();
        List<String> classi = new ArrayList<>();

        Files.walk(SRC).filter(f -> f.toString().endsWith(".java")).forEach(f -> {
            try {
                CompilationUnit cu = StaticJavaParser.parse(f);
                cu.findAll(ClassOrInterfaceDeclaration.class).forEach(dec -> {
                    String nome = dec.getNameAsString();
                    if (dec.isInterface()) {
                        stereotipi.put(nome, "interface " + nome);
                    } else {
                        stereotipi.put(nome, "class \"" + nome + "\"");
                    }
                    String pkg = f.toString().replace(SRC + "", "").replace("\\", "/");
                    // Stereotipo BCE in base al package
                    String st = "";
                    if (pkg.contains("/boundary/")) st = " <<boundary>>";
                    else if (pkg.contains("/control/")) st = " <<control>>";
                    else if (pkg.contains("/entity/")) st = " <<entity>>";
                    else if (pkg.contains("/bean/")) st = " <<bean>>";
                    else if (pkg.contains("/persistence/")) st = " <<dao>>";
                    stereotipi.put(nome, stereo(dec, st));
                    // relazioni: extends/implements
                    List<String> r = new ArrayList<>();
                    dec.getExtendedTypes().forEach(t -> r.add(nome + " -up-|> " + t.getNameAsString()));
                    dec.getImplementedTypes().forEach(t -> r.add(t.getNameAsString() + " <|.. " + nome));
                    relazioni.put(nome, r);
                });
            } catch (Exception ignored) { }
        });

        Files.createDirectories(OUT);
        StringBuilder sb = new StringBuilder("@startuml\nskinparam style strictuml\n");
        stereotipi.values().stream().distinct().forEach(sb::append);
        sb.append("\n");
        relazioni.values().forEach(list -> list.forEach(x -> sb.append(x).append("\n")));
        sb.append("@enduml\n");
        Files.writeString(OUT.resolve("design-level.puml"), sb.toString());
        System.out.println("Generato " + OUT.resolve("design-level.puml"));
    }

    private static String stereo(ClassOrInterfaceDeclaration dec, String st) {
        String nome = dec.getNameAsString();
        if (dec.isInterface()) {
            return "interface \"" + nome + "\"" + st + " { }";
        }
        return "class \"" + nome + "\"" + st + " { }";
    }
}
