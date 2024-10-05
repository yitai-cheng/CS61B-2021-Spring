package gitlet;

import java.io.Serializable;
import java.util.*;

public class CommitGraph implements Serializable {
    private final List<List<Integer>> adj;
    private final Map<String, Integer> commitId2VertexIdMap;
    private final Map<Integer, String> vertexId2CommitIdMap;

    public CommitGraph(String initialCommitId) {
        adj = new ArrayList<>();
        adj.add(new ArrayList<>());
        commitId2VertexIdMap = new HashMap<>();
        commitId2VertexIdMap.put(initialCommitId, 0);

        vertexId2CommitIdMap = new HashMap<>();
        vertexId2CommitIdMap.put(0, initialCommitId);
    }

    /* add vertex */
    public void addVertex(String commitId) {
        int vertexId = adj.size();
        adj.add(new ArrayList<>());
        commitId2VertexIdMap.put(commitId, vertexId);
        vertexId2CommitIdMap.put(vertexId, commitId);
    }

    /* add a directed edge from vertex v to vertex w */
    public void addEdge(String commitId, String parentCommitId) {
        adj.get(commitId2VertexIdMap.get(commitId)).add(commitId2VertexIdMap.get(parentCommitId));
    }

    public List<Integer> getAdj(int v) {
        return adj.get(v);
    }

    public int getVertexId(String commitId) {
        return commitId2VertexIdMap.get(commitId);
    }

    public String getCommitId(int v) {
        return vertexId2CommitIdMap.get(v);
    }
}
