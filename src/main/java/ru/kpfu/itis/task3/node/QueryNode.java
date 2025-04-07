package ru.kpfu.itis.task3.node;

import java.util.Map;
import java.util.Set;

public abstract class QueryNode {
    public abstract Set<String> evaluate(Map<String, Set<String>> index);
}