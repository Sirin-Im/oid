package ru.kpfu.itis.task5;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class SearchEngine {
    private Map<String, List<String>> invertedIndex;
    private Map<String, Map<String, Double>> tfIdfMap;
    private Map<String, Double> idfMap;

    // Загрузка индекса
    public void loadInvertedIndex(String indexPath) throws IOException {
        invertedIndex = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(indexPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(": ");
                if (parts.length == 2) {
                    String term = parts[0];
                    String[] documents = parts[1].split(", ");
                    invertedIndex.put(term, Arrays.asList(documents));
                }
            }
        }
    }

    // Загрузка данных из файлов формата stats_i.txt
    public void loadTfIdfStats(String statsDir) throws IOException {
        tfIdfMap = new HashMap<>();
        idfMap = new HashMap<>();

        File dir = new File(statsDir);
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.getName().startsWith("stats_")) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] parts = line.split("\\s+");
                        if (parts.length >= 3) {
                            String term = parts[0];
                            double idf = Double.parseDouble(parts[1].replace(",", "."));
                            double tfIdf = Double.parseDouble(parts[2].replace(",", "."));

                            // Сохраняем IDF для термина (глобально)
                            idfMap.put(term, idf);

                            // Сохраняем TF-IDF для термина в конкретном документе
                            String docId = file.getName().replace("stats_", "").replace(".txt", "");
                            tfIdfMap.putIfAbsent(term, new HashMap<>());
                            tfIdfMap.get(term).put(docId, tfIdf);
                        }
                    }
                }
            }
        }
    }

    // Поиск релевантных документов
    public List<String> search(String query) {
        String[] terms = query.toLowerCase().split("\\s+");
        Map<String, Double> docScores = new HashMap<>();

        for (String term : terms) {
            if (invertedIndex.containsKey(term)) {
                for (String doc : invertedIndex.get(term)) {
                    String docId = doc.replace("lemmas_", "").replace(".txt", "");
                    double tfIdf = tfIdfMap.getOrDefault(term, Collections.emptyMap())
                            .getOrDefault(docId, 0.0);
                    docScores.put(doc, docScores.getOrDefault(doc, 0.0) + tfIdf);
                }
            }
        }

        // Сортировка документов по убыванию релевантности
        return docScores.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .map(entry -> "page_" + entry.getKey().replace("lemmas_", "").replace(".txt", ""))
                .collect(Collectors.toList());
    }
}
