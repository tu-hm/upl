package org.upl;

import java.util.List;

public class Utils {
    public static  <T extends Comparable<T>> int compareList(List<T> lhs, List<T> rhs) {
        if (lhs == null && rhs == null) return 0;
        if (lhs == null) return -1;
        if (rhs == null) return 1;
        if (lhs.size() != rhs.size()) return Integer.compare(lhs.size(), rhs.size());
        for (int i = 0; i < lhs.size(); i++) {
            T obj1 = lhs.get(i);
            T obj2 = rhs.get(i);
            if (obj1 == null && obj2 == null) continue;
            if (obj1 == null) return -1;
            if (obj2 == null) return 1;
            int cmp = obj1.compareTo(obj2);
            if (cmp != 0) return cmp;
        }
        return 0;
    }
}
