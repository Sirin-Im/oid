package ru.kpfu.itis.task3.node;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TermNode extends QueryNode {

    private final String term;

    public TermNode(String term) {
        this.term = term;
    }

    @Override
    public Set<String> evaluate(Map<String, Set<String>> index) {
        return index.getOrDefault(term, new HashSet<>());
    }
}