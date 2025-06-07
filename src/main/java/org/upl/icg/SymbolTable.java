package org.upl.icg;
import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private Map<String, Integer> symbols;

    public SymbolTable() {
        symbols = new HashMap<>();
    }

    // Adds a symbol to the table
    public void addSymbol(String name, int size) {
        symbols.put(name, size);
    }

    // Retrieves a symbol's value, or null if not found
    public Object getSymbol(String name) {
        return symbols.get(name);
    }

    // Optional: check if a symbol exists
    public boolean hasSymbol(String name) {
        return symbols.containsKey(name);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> entry : symbols.entrySet()) {
            sb.append(entry.getKey()).append(", ");
            if (entry.getValue() == 4) {
                sb.append("int");
            } else if (entry.getValue() == 1) {
                sb.append("bool");
            } else {
                sb.append("unknown"); // Optional: handle other sizes if needed
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
