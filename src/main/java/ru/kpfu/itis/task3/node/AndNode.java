package ru.kpfu.itis.task3.node;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AndNode extends QueryNode {

    private final QueryNode left, right;

    public AndNode(QueryNode l, QueryNode r) {
        left = l;
        right = r;
    }

    @Override
    public Set<String> evaluate(Map<String, Set<String>> index) {
        Set<String> leftSet = new HashSet<>(left.evaluate(index));
        leftSet.retainAll(right.evaluate(index));
        return leftSet;
    }
}