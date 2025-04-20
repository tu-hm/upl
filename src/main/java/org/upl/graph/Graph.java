package org.upl.graph;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

public class Graph {
    private final int V;
    private final List<List<Integer>> E;
    private final List<Integer> visited;
    private final List<Integer> reversedTopo;

    public Graph(int n) {
        V = n;
        E = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            E.add(new ArrayList<>());
        }

        visited = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            visited.add(0);
        }
        reversedTopo = new ArrayList<>();
    }

    public int getV() {
        return V;
    }

    public List<List<Integer>> getE() {
        return E;
    }

    public List<Integer> getReversedTopo() {
        return reversedTopo;
    }

    public void addDirectionalEdge(int u, int v) {
        if (u < 0 || u >= V || v < 0 || v >= V) {
            throw new InvalidParameterException(
                    String.format(
                            "Vertex must be in range 0 -> %d",
                            V - 1
                    )
            );
        }
        E.get(u).add(v);
    }

    private boolean DFS(int u) {
        boolean ok = false;
        visited.set(u, 1);
        for (int v : E.get(u)) {
            if (visited.get(v) == 1) {
                ok = true;
            }

            if (visited.get(v) == 0) {
                ok |= DFS(v);
            }
        }

        visited.set(u, 2);
        reversedTopo.add(u);
        return ok;
    }

    public boolean hasCycle() {
        for (int i = 0; i < V; i++) {
            if (visited.get(i).equals(0)) {
                if (DFS(i)) {
                    return true;
                }
            }
        }

        return false;
    }
}