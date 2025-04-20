package org.upl;
import java.util.Objects;
import java.util.Comparator;

public class Pair implements Comparable<Pair> {
    private final Integer first;
    private final Integer second;

    public Pair(Integer first, Integer second) {
        this.first = first;
        this.second = second;
    }

    public Integer getFirst() {
        return first;
    }

    public Integer getSecond() {
        return second;
    }

    // Lexicographic compareTo: first compare by first, then by second
    @Override
    public int compareTo(Pair other) {
        int cmp = this.first.compareTo(other.first);
        if (cmp != 0) return cmp;
        return this.second.compareTo(other.second);
    }

    // Optional: static comparator if you prefer
    public static final Comparator<Pair> LEXICOGRAPHIC_COMPARATOR =
            Comparator.comparing(Pair::getFirst)
                    .thenComparing(Pair::getSecond);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pair)) return false;
        Pair pair = (Pair) o;
        return Objects.equals(first, pair.first) &&
                Objects.equals(second, pair.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }
}
