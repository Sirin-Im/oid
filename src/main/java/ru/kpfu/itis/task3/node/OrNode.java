package ru.kpfu.itis.task3.node;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class OrNode extends QueryNode {

    private final QueryNode left, right;

    public OrNode(QueryNode l, QueryNode r) {
        left = l;
        right = r;
    }

    @Override
    public Set<String> evaluate(Map<String, Set<String>> index) {
        Set<String> result = new HashSet<>(left.evaluate(index));
        result.addAll(right.evaluate(index));
        return result;
    }
}