package org.upl.icg;

public class Node {
    public String code;

    public Node() {
        code = new String();
    }

    public Node(String _code) {
        code = _code;
    }

    public String toString() {
        return code;
    }
}