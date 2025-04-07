package ru.kpfu.itis.task3.node;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NotNode extends QueryNode {

    private final QueryNode node;
    public NotNode(QueryNode n) {
        node = n;
    }

    @Override
    public Set<String> evaluate(Map<String, Set<String>> index) {
        Set<String> allDocs = new HashSet<>();
        index.values().forEach(allDocs::addAll);
        allDocs.removeAll(node.evaluate(index));
        return allDocs;
    }
}