package ru.kpfu.itis.demo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.kpfu.itis.task5.SearchEngine;

import java.io.IOException;
import java.util.List;

@Controller
public class SearchController {

    private final SearchEngine engine;
    private static final int RESULTS_PER_PAGE = 10;

    public SearchController() throws IOException {
        this.engine = new SearchEngine();
        this.engine.loadInvertedIndex("./result/task3/inverted_index.txt");
        this.engine.loadTfIdfStats("./result/task4/lemmas/");
    }

    @GetMapping("/")
    public String showSearchForm() {
        return "search";
    }

    @GetMapping("/search")
    public String searchGet(@RequestParam String query, @RequestParam(defaultValue = "1") int page, Model model) {
        return performSearch(query, page, model);
    }

    @PostMapping("/search")
    public String searchPost(@RequestParam String query, @RequestParam(defaultValue = "1") int page, Model model) {
        return performSearch(query, page, model);
    }

    private String performSearch(String query, int page, Model model) {
        List<String> allResults = engine.search(query);

        int totalPages = (int) Math.ceil((double) allResults.size() / RESULTS_PER_PAGE);
        page = Math.max(1, Math.min(page, totalPages));

        int startIndex = (page - 1) * RESULTS_PER_PAGE;
        int endIndex = Math.min(startIndex + RESULTS_PER_PAGE, allResults.size());

        List<String> pageResults = allResults.subList(startIndex, endIndex);

        model.addAttribute("query", query);
        model.addAttribute("results", pageResults);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalResults", allResults.size());

        // вычисляем диапазон страниц для пагинации
        int startPage = Math.max(1, page - 2);
        int endPage = Math.min(totalPages, page + 2);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "results";
    }
}