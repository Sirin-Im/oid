package ru.kpfu.itis.task2;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import ru.kpfu.itis.task2.exception.ResourceException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;

public class HtmlParser {
    private static final String PAGE_FILENAME_FORMAT = "./pages/page_%d.html";
    private static final String STOP_WORDS_FILENAME = "stopwords.txt";
    private static final Set<String> STOP_WORDS = read();
    private static final Pattern WORD_PATTERN = Pattern.compile("\\b[a-zA-Z]+\\b");

    public static void main(String[] args) throws IOException {
        //Получения токенов
        Set<String> tokens = tokenizeHtmlFiles();
        //Сохранение токенов
        saveTokens(tokens);

        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        //Распределение токенов по леммам
        Map<String, Set<String>> lemmatizedTokens = lemmatizeTokens(tokens, pipeline);
        //Сохранение лемм
        saveLemmas(lemmatizedTokens);
    }

    private static void saveLemmas(Map<String, Set<String>> lemmasTokens) {
        try (FileWriter writer = new FileWriter("./result/task2/lemmas.txt")) {
            for (Map.Entry<String, Set<String>> entry : lemmasTokens.entrySet()) {
                String lemmaLine = entry.getKey() + ": " + String.join(",", entry.getValue()) + "\n";
                writer.write(lemmaLine);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void saveTokens(Set<String> tokens) {
        try (FileWriter writer = new FileWriter("./result/task2/tokens.txt")) {
            for (String token : tokens) {
                writer.write(token + "\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Set<String> read() {
        try {
            // чтение списка URL из файла в resources
            InputStream inputStream = HtmlParser.class.getClassLoader().getResourceAsStream(HtmlParser.STOP_WORDS_FILENAME);
            if (inputStream == null) {
                throw new IOException("Файл " + HtmlParser.STOP_WORDS_FILENAME + " не найден в resources!");
            }

            // чтение файла построчно
            Scanner scanner = new Scanner(inputStream);
            Set<String> data = new HashSet<>();
            while (scanner.hasNextLine()) {
                data.add(scanner.nextLine());
            }
            scanner.close();
            return data;
        } catch (IOException e) {
            throw new ResourceException();
        }
    }

    //Лемматизация токенов
    private static Map<String, Set<String>> lemmatizeTokens(Set<String> tokens, StanfordCoreNLP pipeline) {
        Map<String, Set<String>> lemmatizedTokens = new TreeMap<>();

        // Создаем текст из токенов
        StringBuilder sb = new StringBuilder();
        for (String token : tokens) {
            sb.append(token).append(" ");
        }
        String text = sb.toString();

        // Аннотируем текст
        Annotation document = new Annotation(text);
        pipeline.annotate(document);

        // Получаем леммы
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String word = token.word();
                String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);

                lemmatizedTokens.computeIfAbsent(lemma, k -> new TreeSet<>()).add(word);
            }
        }

        return lemmatizedTokens;
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
}
