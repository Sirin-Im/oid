package ru.kpfu.itis.task2;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import ru.kpfu.itis.task2.exception.ResourceException;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class HtmlParser {
    private static final String PAGE_FILENAME_FORMAT = "./pages/page_%d.html";
    private static final String STOP_WORDS_FILENAME = "stopwords.txt";
    private static final String ENGLISH_WORDS_FILENAME = "words_alpha.txt";
    private static final Set<String> STOP_WORDS = read(STOP_WORDS_FILENAME);
    private static final Set<String> ENGLISH_WORDS = read(ENGLISH_WORDS_FILENAME);
    private static final Pattern WORD_PATTERN = Pattern.compile("\\b[a-zA-Z]+\\b");

    public static void main(String[] args) throws IOException {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        HtmlParser parser = new HtmlParser();
        parser.parseHtml(pipeline);
    }

    public void parseHtml(Annotator annotator) throws IOException {
        for (int i = 1; i <= 100; i++) {
            //Получение текса со страницы
            String texts = parseText(i);

            //Распределение токенов по леммам
            Map<String, Set<String>> lemmatizedTokens = lemmatizeText(texts, annotator);

            //Сохранение лемм
            saveLemmas(lemmatizedTokens, i);

            Set<String> tokens = new TreeSet<>();

            //Сортирвока токенов
            for (Map.Entry<String,Set<String>> entry : lemmatizedTokens.entrySet()) {
                tokens.addAll(entry.getValue());
            }

            //Сохранение токенов
            saveTokens(tokens, i);
        }
    }

    //Получение текста со сотраницы
    private static String parseText(int pageNumber) throws IOException {
        File input = new File(String.format(PAGE_FILENAME_FORMAT, pageNumber));
        Document doc = Jsoup.parse(input, "UTF-8");
        return doc.body().text();
    }

    private static void saveLemmas(Map<String, Set<String>> lemmasTokens, int pageNumber) {
        try (FileWriter writer = new FileWriter("./result/task2/lemmas/lemmas_" + pageNumber + ".txt")) {
            for (Map.Entry<String, Set<String>> entry : lemmasTokens.entrySet()) {
                String lemmaLine = entry.getKey() + ": " + String.join(",", entry.getValue()) + "\n";
                writer.write(lemmaLine);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void saveTokens(Set<String> tokens, int pageNumber) {
        try (FileWriter writer = new FileWriter("./result/task2/tokens/tokens_" + pageNumber + ".txt")) {
            for (String token : tokens) {
                writer.write(token + "\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //Лемматизация текстов
    private static Map<String, Set<String>> lemmatizeText(String text, Annotator annotator) {
        Map<String, Set<String>> lemmatizedTokens = new TreeMap<>();
        Annotation document = new Annotation(text);
        annotator.annotate(document);

        //Обработка отдлеьных предложений
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {
            processSentence(sentence, lemmatizedTokens);
        }

        return lemmatizedTokens;
    }

    private static void processSentence(CoreMap sentence, Map<String, Set<String>> lemmatizedTokens) {
        for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
            String word = token.word().toLowerCase();
            String lemma = token.lemma().toLowerCase();

            if (!isValidWord(word) || !isValidWord(lemma)) continue;

            lemmatizedTokens.computeIfAbsent(lemma, k -> new TreeSet<>()).add(word);
        }
    }

    //токенезация html файлов
    private static Set<String> tokenizeHtmlFiles() throws IOException {
        Set<String> globalTokens = new TreeSet<>();
        for (int i = 1; i <= 100; i++ ) {
            File input = new File(String.format(PAGE_FILENAME_FORMAT, i));
            Document doc = Jsoup.parse(input, "UTF-8");
            String text = doc.body().text();
            Set<String> htmlTokens = tokenize(text);
            globalTokens.addAll(htmlTokens);
        }
        return globalTokens;
    }

    //токенезация текста одного html файла
    private static Set<String> tokenize(String text) {
        Set<String> tokens = new HashSet<>();

        // Разбиваем текст на слова
        String[] words = text.split("\\s+");

        for (String word : words) {
            word = word.toLowerCase();
            if (STOP_WORDS.contains(word)) continue;
            if (!WORD_PATTERN.matcher(word).matches()) continue;

            tokens.add(word);
        }

        return tokens;
    }

    //Фильтрация мусорных" слов (содержат не толкьо буквы, являются стоп словами, не входят в список английских слов)
    private static boolean isValidWord(String word) {
        return word.matches("^[a-zA-Z]+$") &&
                !word.isEmpty() &&
                !STOP_WORDS.contains(word) &&
                ENGLISH_WORDS.contains(word);
    }

    //Получение ресурса
    private static Set<String> read(String filename) {
        try {
            Set<String> data = new HashSet<>();
            try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/" + filename))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    data.add(line);
                }
            }
            return data;
        } catch (IOException e) {
            throw new ResourceException();
        }
    }
}
