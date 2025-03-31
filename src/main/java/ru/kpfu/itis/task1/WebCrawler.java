package ru.kpfu.itis.task1;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class WebCrawler {

    public static void main(String[] args) {
        // путь к файлу со списком URL для скачивания
        String urlsFilePath = "urls.txt";

        // папка для сохранения страниц
        String outputFolder = "pages";

        try {
            // чтение списка URL из файла в resources
            InputStream inputStream = WebCrawler.class.getClassLoader().getResourceAsStream(urlsFilePath);
            if (inputStream == null) {
                throw new IOException("Файл " + urlsFilePath + " не найден в resources!");
            }

            // чтение файла построчно
            Scanner scanner = new Scanner(inputStream);
            List<String> urls = new ArrayList<>();
            while (scanner.hasNextLine()) {
                urls.add(scanner.nextLine());
            }
            scanner.close();

            // создание папки для сохранения страниц
            if (!Files.exists(Paths.get(outputFolder))) {
                Files.createDirectory(Paths.get(outputFolder));
            }

            // файл для записи индекса
            FileWriter indexWriter = new FileWriter("index.txt");

            // скачивание каждой страницы
            for (int i = 0; i < urls.size(); i++) {
                String url = urls.get(i);
                try {
                    // загрузка HTML-страницы
                    Document doc = Jsoup.connect(url).get();

                    // сохранение HTML-страницы в файл
                    String fileName = outputFolder + "/page_" + (i + 1) + ".html";
                    FileWriter writer = new FileWriter(fileName);
                    writer.write(doc.html());
                    writer.close();

                    // запись в index.txt
                    indexWriter.write((i + 1) + ": " + url + "\n");

                    System.out.println("Страница " + (i + 1) + " сохранена: " + fileName);
                } catch (IOException e) {
                    System.err.println("Ошибка при скачивании страницы " + url + ": " + e.getMessage());
                }
            }

            // закрытие index.txt
            indexWriter.close();
        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла или создании папки: " + e.getMessage());
        }
    }
}