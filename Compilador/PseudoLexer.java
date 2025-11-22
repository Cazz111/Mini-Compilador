import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PseudoLexer {
    private String input;
    private int position;
    private int line;
    private static Map<String, String> palabrasReservadas;
    
    static {
        palabrasReservadas = new HashMap<>();
        palabrasReservadas.put("inicio-programa", "INICIOPROGRAMA");
        palabrasReservadas.put("fin-programa", "FINPROGRAMA");
        palabrasReservadas.put("variables", "VARIABLES");
        palabrasReservadas.put("entero", "entero");
        palabrasReservadas.put("real", "real");
        palabrasReservadas.put("cadena", "cadena");
        palabrasReservadas.put("logico", "logico");
        palabrasReservadas.put("leer", "LEER");
        palabrasReservadas.put("escribir", "ESCRIBIR");
        palabrasReservadas.put("si", "SI");
        palabrasReservadas.put("entonces", "ENTONCES");
        palabrasReservadas.put("fin-si", "FINSI");
        palabrasReservadas.put("mientras", "MIENTRAS");
        palabrasReservadas.put("hacer", "HACER");
        palabrasReservadas.put("fin-mientras", "FINMIENTRAS");
        palabrasReservadas.put("repetir", "REPETIR");
        palabrasReservadas.put("hasta", "HASTA");
    }
    
    public static class Token {
        private String tipo;
        private String lexema;
        private int linea;
        
        public Token(String tipo, String lexema, int linea) {
            this.tipo = tipo;
            this.lexema = lexema;
            this.linea = linea;
        }
        
        public String getTipo() { return tipo; }
        public String getLexema() { return lexema; }
        public int getLinea() { return linea; }
        
        @Override
        public String toString() {
            return "Token(" + tipo + ", '" + lexema + "', linea " + linea + ")";
        }
    }
    
    public PseudoLexer(String input) {
        this.input = input;
        this.position = 0;
        this.line = 1;
    }
    
    public ArrayList<Token> tokenize() {
        ArrayList<Token> tokens = new ArrayList<>();
        
        while (position < input.length()) {
            char currentChar = input.charAt(position);
            
            if (Character.isWhitespace(currentChar)) {
                if (currentChar == '\n') line++;
                position++;
                continue;
            }
            
            if (Character.isLetter(currentChar)) {
                String word = readWord();
                if (palabrasReservadas.containsKey(word)) {
                    tokens.add(new Token(palabrasReservadas.get(word), word, line));
                } else {
                    tokens.add(new Token("VARIABLE", word, line));
                }
                continue;
            }
            
            if (Character.isDigit(currentChar)) {
                String number = readNumber();
                if (number.contains(".")) {
                    tokens.add(new Token("REAL", number, line));
                } else {
                    tokens.add(new Token("NUMERO", number, line));
                }
                continue;
            }
            
            if (currentChar == ':') {
                tokens.add(new Token("DOSPUNTOS", ":", line));
                position++;
                continue;
            }
            
            if (currentChar == ',') {
                tokens.add(new Token("COMA", ",", line));
                position++;
                continue;
            }
            
            if (currentChar == '=') {
                if (position + 1 < input.length() && input.charAt(position + 1) == '=') {
                    tokens.add(new Token("IGUALIGUAL", "==", line));
                    position += 2;
                } else {
                    tokens.add(new Token("IGUAL", "=", line));
                    position++;
                }
                continue;
            }
            
            if (currentChar == '>') {
                if (position + 1 < input.length() && input.charAt(position + 1) == '=') {
                    tokens.add(new Token("MAYORIGUAL", ">=", line));
                    position += 2;
                } else {
                    tokens.add(new Token("MAYOR", ">", line));
                    position++;
                }
                continue;
            }
            
            if (currentChar == '<') {
                if (position + 1 < input.length() && input.charAt(position + 1) == '=') {
                    tokens.add(new Token("MENORIGUAL", "<=", line));
                    position += 2;
                } else if (position + 1 < input.length() && input.charAt(position + 1) == '>') {
                    tokens.add(new Token("DIFERENTE", "<>", line));
                    position += 2;
                } else {
                    tokens.add(new Token("MENOR", "<", line));
                    position++;
                }
                continue;
            }
            
            if (currentChar == '+') {
                tokens.add(new Token("MAS", "+", line));
                position++;
                continue;
            }
            
            if (currentChar == '-') {
                tokens.add(new Token("MENOS", "-", line));
                position++;
                continue;
            }
            
            if (currentChar == '*') {
                tokens.add(new Token("POR", "*", line));
                position++;
                continue;
            }
            
            if (currentChar == '/') {
                tokens.add(new Token("DIVISION", "/", line));
                position++;
                continue;
            }
            
            if (currentChar == '(') {
                tokens.add(new Token("PARENTESISIZQ", "(", line));
                position++;
                continue;
            }
            
            if (currentChar == ')') {
                tokens.add(new Token("PARENTESISDER", ")", line));
                position++;
                continue;
            }
            
            System.err.println("Caracter no reconocido: '" + currentChar + "' en línea " + line);
            position++;
        }
        
        return tokens;
    }
    
    private String readWord() {
        StringBuilder word = new StringBuilder();
        while (position < input.length()) {
            char c = input.charAt(position);
            if (Character.isLetterOrDigit(c) || c == '-' || c == '_') {
                word.append(c);
                position++;
            } else break;
        }
        return word.toString();
    }
    
    private String readNumber() {
        StringBuilder number = new StringBuilder();
        while (position < input.length()) {
            char c = input.charAt(position);
            if (Character.isDigit(c) || c == '.') {
                number.append(c);
                position++;
            } else break;
        }
        return number.toString();
    }
}