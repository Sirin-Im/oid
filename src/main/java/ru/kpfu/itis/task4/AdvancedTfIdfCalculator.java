package ru.kpfu.itis.task4;

import java.util.*;
import java.io.*;
import java.nio.file.*;

public class AdvancedTfIdfCalculator {
    private final Map<String, Set<String>> globalLemmasMap; // Лемма -> все её формы
    private final Map<String, String> wordToLemma; // Слово -> его лемма
    
    // Для статистики по коллекции
    private final Map<String, Integer> termDocCount; // В скольких документах встречается термин
    private final Map<String, Integer> lemmaDocCount; // В скольких документах встречается лемма
    
    // Пути к файлам
    private static final String TOKENS_DIR = "./result/task2/tokens/tokens_%d.txt";
    private static final String LEMMAS_DIR = "./result/task2/lemmas/lemmas_%d.txt";
    private static final String RESULT_TOKENS_DIR = "./result/task4/tokens/stats_%d.txt";
    private static final String RESULT_LEMMAS_DIR = "./result/task4/lemmas/stats_%d.txt";

    public static void main(String[] args) throws IOException {
        AdvancedTfIdfCalculator calculator = new AdvancedTfIdfCalculator();
        calculator.initialize();
        calculator.calculateAll();
    }

    public AdvancedTfIdfCalculator() {
        globalLemmasMap = new HashMap<>();
        wordToLemma = new HashMap<>();
        termDocCount = new HashMap<>();
        lemmaDocCount = new HashMap<>();
    }

    public void initialize() throws IOException {
        buildGlobalLemmasMap();
    }
    
    private void buildGlobalLemmasMap() throws IOException {
        for (int i = 1; i <= 100; i++) {
            Path lemmaPath = Paths.get(String.format(LEMMAS_DIR, i));
            List<String> lines = Files.readAllLines(lemmaPath);
            for (String line : lines) {
                String[] parts = line.split(":");
                if (parts.length < 2) continue;

                String lemma = parts[0].trim();
                String[] forms = parts[1].trim().split(",");

                // Добавляем в глобальную карту лемм
                globalLemmasMap.putIfAbsent(lemma, new HashSet<>());
                globalLemmasMap.get(lemma).add(lemma); // Лемма тоже считается своей формой

                for (String form : forms) {
                    String trimmedForm = form.trim();
                    globalLemmasMap.get(lemma).add(trimmedForm);
                    wordToLemma.put(trimmedForm, lemma);
                }
            }
        }
    }
    
    public void calculateAll() throws IOException {
        // 1. Подсчет в скольких документах встречается каждый термин и лемма
        countDocumentOccurrences();
        
        // 2. Расчет TF-IDF для каждого документа
        for (int i = 1; i <= 100; i++) {
            // Читаем токены документа
            List<String> tokens = Files.readAllLines(Paths.get(String.format(TOKENS_DIR, i)));

            // Рассчитываем статистики
            Map<String, Double> termTf = calculateTermTf(tokens);
            Map<String, Double> lemmaTf = calculateLemmaTf(tokens);

            // Записываем результаты
            saveStatistics(i, termTf, lemmaTf);
        }
    }
    
    private void countDocumentOccurrences() throws IOException {
        for (int i = 1; i <= 100; i++) {
            List<String> tokens = Files.readAllLines(Paths.get(String.format(TOKENS_DIR, i)));
            Set<String> uniqueTerms = new HashSet<>(tokens);
            Set<String> uniqueLemmas = new HashSet<>();

            // Для каждого термина находим его лемму
            for (String term : uniqueTerms) {
                String lemma = wordToLemma.get(term);
                if (lemma != null) {
                    uniqueLemmas.add(lemma);
                }
            }

            // Обновляем счетчики документов для терминов
            for (String term : uniqueTerms) {
                termDocCount.put(term, termDocCount.getOrDefault(term, 0) + 1);
            }

            // Обновляем счетчики документов для лемм
            for (String lemma : uniqueLemmas) {
                lemmaDocCount.put(lemma, lemmaDocCount.getOrDefault(lemma, 0) + 1);
            }
        }
    }
    
    private Map<String, Double> calculateTermTf(List<String> tokens) {
        Map<String, Integer> termFreq = new HashMap<>();
        int totalTerms = tokens.size();
        
        // Считаем частоту каждого термина
        for (String term : tokens) {
            termFreq.put(term, termFreq.getOrDefault(term, 0) + 1);
        }
        
        // Вычисляем TF
        Map<String, Double> tf = new HashMap<>();
        for (Map.Entry<String, Integer> entry : termFreq.entrySet()) {
            tf.put(entry.getKey(), (double) entry.getValue() / totalTerms);
        }
        
        return tf;
    }
    
    private Map<String, Double> calculateLemmaTf(List<String> tokens) {
        Map<String, Integer> lemmaFreq = new HashMap<>();
        int totalLemmas = 0;
        
        // Считаем частоту каждой леммы
        for (String term : tokens) {
            String lemma = wordToLemma.get(term);
            if (lemma != null) {
                lemmaFreq.put(lemma, lemmaFreq.getOrDefault(lemma, 0) + 1);
                totalLemmas++;
            }
        }
        
        // Вычисляем TF
        Map<String, Double> tf = new HashMap<>();
        for (Map.Entry<String, Integer> entry : lemmaFreq.entrySet()) {
            tf.put(entry.getKey(), (double) entry.getValue() / totalLemmas);
        }
        
        return tf;
    }
    
    private double calculateTermIdf(String term) {
        int docsWithTerm = termDocCount.getOrDefault(term, 0);
        return Math.log((double) 100 / (1 + docsWithTerm));
    }
    
    private double calculateLemmaIdf(String lemma) {
        int docsWithLemma = lemmaDocCount.getOrDefault(lemma, 0);
        return Math.log((double) 100 / (1 + docsWithLemma));
    }
    
    private void saveStatistics(int docNum, Map<String, Double> termTf, Map<String, Double> lemmaTf) {
        // Сохраняем данные по токенам
        try (PrintWriter tokenWriter = new PrintWriter(String.format(RESULT_TOKENS_DIR, docNum))) {
            termTf.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey()) // Сортируем по термину
                    .forEach(entry -> {
                        String term = entry.getKey();
                        double tf = entry.getValue();
                        double idf = calculateTermIdf(term);
                        double tfIdf = tf * idf;
                        tokenWriter.printf("%s %.6f %.6f%n", term, idf, tfIdf);
                    });
        } catch (FileNotFoundException e) {
            System.err.println("Error writing token results for document: " + docNum);
            e.printStackTrace();
        }

        // Сохраняем данные по леммам
        try (PrintWriter lemmaWriter = new PrintWriter(String.format(RESULT_LEMMAS_DIR, docNum))) {
            lemmaTf.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey()) // Сортируем по лемме
                    .forEach(entry -> {
                        String lemma = entry.getKey();
                        double tf = entry.getValue();
                        double idf = calculateLemmaIdf(lemma);
                        double tfIdf = tf * idf;
                        lemmaWriter.printf("%s %.6f %.6f%n", lemma, idf, tfIdf);
                    });
        } catch (FileNotFoundException e) {
            System.err.println("Error writing lemma results for document: " + docNum);
            e.printStackTrace();
        }
    }
}
