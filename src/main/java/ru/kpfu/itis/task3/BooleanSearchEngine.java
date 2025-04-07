package ru.kpfu.itis.task3;

import ru.kpfu.itis.task3.node.QueryNode;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class BooleanSearchEngine {
    private final Map<String, Set<String>> invertedIndex = new HashMap<>();

    // построение инвертированного индекса
    public void buildIndex(String directoryPath) throws IOException {
        Files.list(Paths.get(directoryPath))
                .filter(path -> path.toString().endsWith(".txt"))
                .forEach(path -> {
                    String docName = path.getFileName().toString();
                    try {
                        List<String> lines = Files.readAllLines(path);
                        for (String line : lines) {
                            String[] parts = line.split(":");
                            if (parts.length < 2) continue;

                            String[] lemmas = parts[1].split(",");
                            for (String lemma : lemmas) {
                                lemma = lemma.trim().toLowerCase();
                                if (!lemma.isEmpty()) {
                                    invertedIndex.computeIfAbsent(lemma, k -> new HashSet<>()).add(docName);
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    // сохранение индекса в файл
    public void saveIndexToFile(String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            for (Map.Entry<String, Set<String>> entry : invertedIndex.entrySet()) {
                String term = entry.getKey();
                String docs = String.join(", ", entry.getValue());
                writer.println(term + ": " + docs);
            }
            System.out.println("Индекс успешно сохранён в файл: " + filePath);
        } catch (IOException e) {
            System.out.println("Ошибка при сохранении индекса: " + e.getMessage());
        }
    }

    // парсинг и выполнение запроса
    public Set<String> search(String query) {
        QueryNode parsed = new QueryParser(query).parse();
        return parsed.evaluate(invertedIndex);
    }

    public static void main(String[] args) throws IOException {
        BooleanSearchEngine engine = new BooleanSearchEngine();

        // путь к папке с файлами лемм
        engine.buildIndex("result/task2/lemmas");

        engine.saveIndexToFile("result/task3/inverted_index.txt");

        Scanner scanner = new Scanner(System.in);
        System.out.println("Введите булевый запрос (используйте AND, OR, NOT, скобки или 'exit' для выхода):");

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().toLowerCase().trim();

            if (input.equals("exit")) {
                System.out.println("Выход из программы");
                break;
            }

            try {
                Set<String> result = engine.search(input);
                if (result.isEmpty()) {
                    System.out.println("Ничего не найдено");
                } else {
                    System.out.println("Найдено в документах:");
                    result.forEach(System.out::println);
                }
            } catch (Exception e) {
                System.out.println("Ошибка в запросе: " + e.getMessage());
            }
        }
    }

}