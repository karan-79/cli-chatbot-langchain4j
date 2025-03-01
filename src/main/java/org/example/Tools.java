package org.example;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.web.search.WebSearchOrganicResult;
import dev.langchain4j.web.search.tavily.TavilyWebSearchEngine;

import java.util.List;

public class Tools {


    @Tool("Searches the internet for relevant information for given input query")
    public List<WebSearchOrganicResult> searchInternet(String query) {
        TavilyWebSearchEngine webSearchEngine = TavilyWebSearchEngine.builder()
                .apiKey(System.getenv("TAVILY_API_KEY"))
                .build();

        var webSearchResults= webSearchEngine.search(query);
        return webSearchResults.results().subList(0, Math.min(4, webSearchResults.results().size()));
    }

}
