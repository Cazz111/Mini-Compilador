import java.util.*;

public class SymbolTable implements Scope {
    Map<String, Symbol> symbols = new HashMap<String, Symbol>();
    
    public SymbolTable() {
        initTypeSystem();
    }
    
    protected void initTypeSystem() {
        define(new BuiltInTypeSymbol("entero"));
        define(new BuiltInTypeSymbol("real"));
        define(new BuiltInTypeSymbol("cadena"));
        define(new BuiltInTypeSymbol("logico"));
    }
    
    @Override
    public String getScopeName() {
        return "global";
    }
    
    @Override
    public Scope getEnclosingScope() {
        return null;
    }
    
    @Override
    public void define(Symbol sym) {
        symbols.put(sym.name, sym);
        sym.scope = this;
    }
    
    @Override
    public Symbol resolve(String name) {
        return symbols.get(name);
    }
    
    @Override
    public String toString() {
        return getScopeName() + ":" + symbols;
    }
}