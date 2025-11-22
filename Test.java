import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Test {
    public static void main(String[] args) {
        try {
            String contenido = new String(Files.readAllBytes(Paths.get("test.txt")));
            
            PseudoLexer lexer = new PseudoLexer(contenido);
            ArrayList<PseudoLexer.Token> tokens = lexer.tokenize();
            
            System.out.println("========================================");
            System.out.println("  COMPILADOR DE PSEUDOCODIGO A ASM");
            System.out.println("========================================");
            System.out.println("\n********** Tokens Generados **********");
            for (PseudoLexer.Token token : tokens) {
                System.out.println(token);
            }
            
            PseudoParser parser = new PseudoParser();
            boolean exito = parser.parse(tokens);
            
            if (exito) {
                System.out.println("\n========================================");
                System.out.println("  COMPILACION EXITOSA");
                System.out.println("========================================");
            } else {
                System.out.println("\n========================================");
                System.out.println("  ERROR EN COMPILACION");
                System.out.println("========================================");
            }
        } catch (IOException e) {
            System.err.println("Error al leer archivo: " + e.getMessage());
        }
    }
}
