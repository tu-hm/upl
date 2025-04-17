package org.upl.parser.grammar;

public abstract class Symbol implements Comparable<Symbol> {
    public final String value;
    public final Object object;
    public Symbol(String value) {
        this.value = value; this.object = null;
    }

    public Symbol(String value, Object object) {
        this.value = value;
        this.object = object;
    }

    @Override
    public int compareTo(Symbol symbol) {
        return value.compareTo(symbol.value);
    }
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Symbol x)) return false;
        return this.value.equals(x.value);
    }
    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}