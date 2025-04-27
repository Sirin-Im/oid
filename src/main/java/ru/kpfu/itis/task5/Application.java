package ru.kpfu.itis.task5;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class Application {
    public static void main(String[] args) throws IOException {
        SearchEngine engine = new SearchEngine();
        try {
            engine.loadInvertedIndex("./result/task3/inverted_index.txt");
            engine.loadTfIdfStats("./result/task4/lemmas/");
            
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("Enter search query (or 'exit' to quit): ");
                String query = scanner.nextLine();
                if (query.equalsIgnoreCase("exit")) {
                    break;
                }
                
                List<String> results = engine.search(query);
                System.out.println("Search results:");
                for (int i = 0; i < Math.min(results.size(), 10); i++) {
                    System.out.println((i+1) + ". " + results.get(i));
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading index: " + e.getMessage());
        }
    }
}
