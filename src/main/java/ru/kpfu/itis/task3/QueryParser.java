package ru.kpfu.itis.task3;

import ru.kpfu.itis.task3.node.AndNode;
import ru.kpfu.itis.task3.node.NotNode;
import ru.kpfu.itis.task3.node.OrNode;
import ru.kpfu.itis.task3.node.QueryNode;
import ru.kpfu.itis.task3.node.TermNode;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class QueryParser {
    private final List<String> tokens;
    private int pos = 0;

    public QueryParser(String input) {
        this.tokens = tokenize(input);
    }

    private List<String> tokenize(String input) {
        List<String> result = new ArrayList<>();
        Matcher m = Pattern.compile("\\(|\\)|AND|OR|NOT|[а-яa-z0-9]+", Pattern.CASE_INSENSITIVE).matcher(input);
        while (m.find()) result.add(m.group());
        return result;
    }

    public QueryNode parse() {
        return parseOr();
    }

    private QueryNode parseOr() {
        QueryNode node = parseAnd();
        while (pos < tokens.size() && tokens.get(pos).equalsIgnoreCase("OR")) {
            pos++;
            node = new OrNode(node, parseAnd());
        }
        return node;
    }

    private QueryNode parseAnd() {
        QueryNode node = parseNot();
        while (pos < tokens.size() && tokens.get(pos).equalsIgnoreCase("AND")) {
            pos++;
            node = new AndNode(node, parseNot());
        }
        return node;
    }

    private QueryNode parseNot() {
        if (pos < tokens.size() && tokens.get(pos).equalsIgnoreCase("NOT")) {
            pos++;
            return new NotNode(parseAtom());
        }
        return parseAtom();
    }

    private QueryNode parseAtom() {
        String token = tokens.get(pos);
        if (token.equals("(")) {
            pos++;
            QueryNode node = parseOr();
            pos++; // пропускаем ")"
            return node;
        }
        pos++;
        return new TermNode(token);
    }
}