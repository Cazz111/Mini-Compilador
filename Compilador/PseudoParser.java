import java.util.ArrayList;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;

public class PseudoParser {
    ArrayList<PseudoLexer.Token> tokens;
    int tokenIndex = 0;
    SymbolTable tabla;
    PrintWriter asmWriter;
    int labelCount = 0;
    
    public boolean parse(ArrayList<PseudoLexer.Token> tokens) {
        this.tokens = tokens;
        tabla = new SymbolTable();
        tabla.initTypeSystem();
        
        try {
            asmWriter = new PrintWriter(new FileWriter("salida.asm"));
            System.out.println("\n\n********** Reglas de producción **********\n");
            boolean resultado = programa();
            asmWriter.close();
            
            if (resultado) {
                System.out.println("\n\n********** Tabla de Símbolos **********");
                System.out.println(tabla);
                System.out.println("\n********** Archivo generado **********");
                System.out.println("- salida.asm");
            }
            return resultado;
        } catch (IOException e) {
            System.err.println("Error al crear archivo: " + e.getMessage());
            return false;
        }
    }
    
    private String newLabel() {
        return "L" + (labelCount++);
    }
    
    private boolean programa() {
        System.out.println("<Programa> --> inicio-programa <Declaraciones> <Enunciados> fin-programa");
        
        asmWriter.println("; Compilador de Pseudocodigo a Ensamblador x86");
        asmWriter.println("; Generado automaticamente");
        asmWriter.println("");
        asmWriter.println(".386");
        asmWriter.println(".model flat, stdcall");
        asmWriter.println("option casemap:none");
        asmWriter.println("");
        asmWriter.println("include \\masm32\\include\\windows.inc");
        asmWriter.println("include \\masm32\\include\\kernel32.inc");
        asmWriter.println("include \\masm32\\include\\masm32.inc");
        asmWriter.println("include \\masm32\\include\\msvcrt.inc");
        asmWriter.println("");
        asmWriter.println("includelib \\masm32\\lib\\kernel32.lib");
        asmWriter.println("includelib \\masm32\\lib\\masm32.lib");
        asmWriter.println("includelib \\masm32\\lib\\msvcrt.lib");
        asmWriter.println("");
        asmWriter.println(".data");
        asmWriter.println("    formato_out DB \"%d\", 10, 0");
        asmWriter.println("    formato_in DB \"%d\", 0");
        asmWriter.println("    buffer DB 100 DUP(??)");
        
        if (match("INICIOPROGRAMA")) {
            if (declaracionVariables()) {
                asmWriter.println("");
                asmWriter.println(".code");
                asmWriter.println("start:");
                
                if (enunciados()) {
                    if (match("FINPROGRAMA")) {
                        if (tokenIndex == tokens.size()) {
                            asmWriter.println("");
                            asmWriter.println("    invoke ExitProcess, 0");
                            asmWriter.println("end start");
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    
    private boolean declaracionVariables() {
        if (match("VARIABLES")) {
            if (tipo()) {
                PseudoLexer.Token tipoToken = tokens.get(tokenIndex - 1);
                String nombreTipo = tipoToken.getLexema();
                BuiltInTypeSymbol bit = (BuiltInTypeSymbol) tabla.resolve(nombreTipo);
                
                if (bit == null) {
                    System.err.println("Error: Tipo '" + nombreTipo + "' no definido");
                    return false;
                }
                
                if (match("DOSPUNTOS")) {
                    if (listaVariables(bit)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private boolean listaVariables(BuiltInTypeSymbol tipo) {
        if (match("VARIABLE")) {
            PseudoLexer.Token varToken = tokens.get(tokenIndex - 1);
            String nombreVar = varToken.getLexema();
            VariableSymbol vs = new VariableSymbol(nombreVar, tipo);
            tabla.define(vs);
            
            if (tipo.getName().equals("real")) {
                asmWriter.println("    " + nombreVar + " REAL4 ?");
            } else {
                asmWriter.println("    " + nombreVar + " DWORD ?");
            }
            
            if (listaVariablesPrima(tipo)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean listaVariablesPrima(BuiltInTypeSymbol tipo) {
        if (match("COMA")) {
            if (match("VARIABLE")) {
                PseudoLexer.Token varToken = tokens.get(tokenIndex - 1);
                String nombreVar = varToken.getLexema();
                VariableSymbol vs = new VariableSymbol(nombreVar, tipo);
                tabla.define(vs);
                
                if (tipo.getName().equals("real")) {
                    asmWriter.println("    " + nombreVar + " REAL4 ?");
                } else {
                    asmWriter.println("    " + nombreVar + " DWORD ?");
                }
                return listaVariablesPrima(tipo);
            }
            return false;
        }
        return true;
    }
    
    private boolean enunciados() {
        if (enunciado()) {
            if (enunciadosPrima()) {
                return true;
            }
        }
        return false;
    }
    
    private boolean enunciadosPrima() {
        if (tokenIndex < tokens.size()) {
            PseudoLexer.Token currentToken = tokens.get(tokenIndex);
            if (currentToken.getTipo().equals("VARIABLE") || 
                currentToken.getTipo().equals("LEER") ||
                currentToken.getTipo().equals("ESCRIBIR") ||
                currentToken.getTipo().equals("SI") ||
                currentToken.getTipo().equals("MIENTRAS") ||
                currentToken.getTipo().equals("REPETIR")) {
                return enunciados();
            }
        }
        return true;
    }
    
    private boolean enunciado() {
        if (tokenIndex < tokens.size()) {
            PseudoLexer.Token currentToken = tokens.get(tokenIndex);
            
            if (currentToken.getTipo().equals("VARIABLE")) {
                return enunciadoAsignacion();
            } else if (currentToken.getTipo().equals("LEER")) {
                return enunciadoLeer();
            } else if (currentToken.getTipo().equals("ESCRIBIR")) {
                return enunciadoEscribir();
            } else if (currentToken.getTipo().equals("SI")) {
                return enunciadoSi();
            } else if (currentToken.getTipo().equals("MIENTRAS")) {
                return enunciadoMientras();
            } else if (currentToken.getTipo().equals("REPETIR")) {
                return enunciadoRepetir();
            }
        }
        return false;
    }
    
    private boolean enunciadoAsignacion() {
        System.out.println("<Asignacion> --> <Variable> = <Expresion>");
        
        if (match("VARIABLE")) {
            PseudoLexer.Token varToken = tokens.get(tokenIndex - 1);
            String nombreVar = varToken.getLexema();
            
            if (tabla.resolve(nombreVar) == null) {
                System.err.println("Error línea " + varToken.getLinea() + ": Variable '" + nombreVar + "' no declarada");
                return false;
            }
            
            if (match("IGUAL")) {
                if (expresion()) {
                    asmWriter.println("    mov " + nombreVar + ", eax");
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean enunciadoLeer() {
        System.out.println("<Leer> --> leer <Variable>");
        
        if (match("LEER")) {
            if (match("VARIABLE")) {
                PseudoLexer.Token varToken = tokens.get(tokenIndex - 1);
                String nombreVar = varToken.getLexema();
                
                if (tabla.resolve(nombreVar) == null) {
                    System.err.println("Error línea " + varToken.getLinea() + ": Variable '" + nombreVar + "' no declarada");
                    return false;
                }
                
                asmWriter.println("    invoke crt_scanf, ADDR formato_in, ADDR " + nombreVar);
                return true;
            }
        }
        return false;
    }
    
    private boolean enunciadoEscribir() {
        System.out.println("<Escribir> --> escribir <Expresion>");
        
        if (match("ESCRIBIR")) {
            if (expresion()) {
                asmWriter.println("    invoke crt_printf, ADDR formato_out, eax");
                return true;
            }
        }
        return false;
    }
    
    private boolean enunciadoSi() {
        System.out.println("<Si> --> si <Condicion> entonces <Enunciados> fin-si");
        
        if (match("SI")) {
            String labelFin = newLabel();
            
            if (condicion(labelFin)) {
                if (match("ENTONCES")) {
                    if (enunciados()) {
                        if (match("FINSI")) {
                            asmWriter.println(labelFin + ":");
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    
    private boolean enunciadoMientras() {
        System.out.println("<Mientras> --> mientras <Condicion> hacer <Enunciados> fin-mientras");
        
        if (match("MIENTRAS")) {
            String labelInicio = newLabel();
            String labelFin = newLabel();
            
            asmWriter.println(labelInicio + ":");
            
            if (condicion(labelFin)) {
                if (match("HACER")) {
                    if (enunciados()) {
                        if (match("FINMIENTRAS")) {
                            asmWriter.println("    jmp " + labelInicio);
                            asmWriter.println(labelFin + ":");
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    
    private boolean enunciadoRepetir() {
        System.out.println("<Repetir> --> repetir <Enunciados> hasta <Condicion>");
        
        if (match("REPETIR")) {
            String labelInicio = newLabel();
            asmWriter.println(labelInicio + ":");
            
            if (enunciados()) {
                if (match("HASTA")) {
                    if (condicionRepetir(labelInicio)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private boolean expresion() {
        if (termino()) {
            if (expresionPrima()) {
                return true;
            }
        }
        return false;
    }
    
    private boolean expresionPrima() {
        if (match("MAS")) {
            asmWriter.println("    push eax");
            if (termino()) {
                asmWriter.println("    pop ebx");
                asmWriter.println("    add eax, ebx");
                return expresionPrima();
            }
            return false;
        } else if (match("MENOS")) {
            asmWriter.println("    push eax");
            if (termino()) {
                asmWriter.println("    pop ebx");
                asmWriter.println("    sub ebx, eax");
                asmWriter.println("    mov eax, ebx");
                return expresionPrima();
            }
            return false;
        }
        return true;
    }
    
    private boolean termino() {
        if (factor()) {
            if (terminoPrima()) {
                return true;
            }
        }
        return false;
    }
    
    private boolean terminoPrima() {
        if (match("POR")) {
            asmWriter.println("    push eax");
            if (factor()) {
                asmWriter.println("    pop ebx");
                asmWriter.println("    imul eax, ebx");
                return terminoPrima();
            }
            return false;
        } else if (match("DIVISION")) {
            asmWriter.println("    push eax");
            if (factor()) {
                asmWriter.println("    mov ebx, eax");
                asmWriter.println("    pop eax");
                asmWriter.println("    cdq");
                asmWriter.println("    idiv ebx");
                return terminoPrima();
            }
            return false;
        }
        return true;
    }
    
    private boolean factor() {
        if (match("VARIABLE")) {
            PseudoLexer.Token varToken = tokens.get(tokenIndex - 1);
            String nombreVar = varToken.getLexema();
            
            if (tabla.resolve(nombreVar) == null) {
                System.err.println("Error línea " + varToken.getLinea() + ": Variable '" + nombreVar + "' no declarada");
                return false;
            }
            
            asmWriter.println("    mov eax, " + nombreVar);
            return true;
        }
        
        if (match("NUMERO")) {
            PseudoLexer.Token numToken = tokens.get(tokenIndex - 1);
            asmWriter.println("    mov eax, " + numToken.getLexema());
            return true;
        }
        
        if (match("REAL")) {
            PseudoLexer.Token realToken = tokens.get(tokenIndex - 1);
            asmWriter.println("    mov eax, " + realToken.getLexema());
            return true;
        }
        
        if (match("PARENTESISIZQ")) {
            if (expresion()) {
                if (match("PARENTESISDER")) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private boolean condicion(String labelFalso) {
        if (expresion()) {
            asmWriter.println("    push eax");
            String op = "";
            
            if (match("MAYOR")) op = "jle";
            else if (match("MENOR")) op = "jge";
            else if (match("MAYORIGUAL")) op = "jl";
            else if (match("MENORIGUAL")) op = "jg";
            else if (match("IGUALIGUAL")) op = "jne";
            else if (match("DIFERENTE")) op = "je";
            else return false;
            
            if (expresion()) {
                asmWriter.println("    pop ebx");
                asmWriter.println("    cmp ebx, eax");
                asmWriter.println("    " + op + " " + labelFalso);
                return true;
            }
        }
        return false;
    }
    
    private boolean condicionRepetir(String labelInicio) {
        if (expresion()) {
            asmWriter.println("    push eax");
            String op = "";
            
            if (match("MAYOR")) op = "jle";
            else if (match("MENOR")) op = "jge";
            else if (match("MAYORIGUAL")) op = "jl";
            else if (match("MENORIGUAL")) op = "jg";
            else if (match("IGUALIGUAL")) op = "jne";
            else if (match("DIFERENTE")) op = "je";
            else return false;
            
            if (expresion()) {
                asmWriter.println("    pop ebx");
                asmWriter.println("    cmp ebx, eax");
                asmWriter.println("    " + op + " " + labelInicio);
                return true;
            }
        }
        return false;
    }
    
    private boolean tipo() {
        return match("entero") || match("real") || match("cadena") || match("logico");
    }
    
    private boolean match(String expectedType) {
        if (tokenIndex < tokens.size()) {
            PseudoLexer.Token currentToken = tokens.get(tokenIndex);
            if (currentToken.getTipo().equals(expectedType)) {
                tokenIndex++;
                return true;
            }
        }
        return false;
    }
}