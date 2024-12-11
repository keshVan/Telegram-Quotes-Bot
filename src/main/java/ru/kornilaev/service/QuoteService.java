package ru.kornilaev.service;

import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class QuoteService {
    private static final String API_URL = "http://api.forismatic.com/api/1.0/";

    public static String getRandomQuote() {
        String requestUrl =  API_URL + "?method=getQuote&format=json&lang=ru";
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(requestUrl))
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return parseQuote(response.body());
            }
            else {
                return "Ошибка при получении цитаты";
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return "Ошибка при получении цитаты";
    }

    public static String parseQuote(String response) {
        StringBuilder out = new StringBuilder("«");

        JSONObject quote = new JSONObject(response);
        String quoteText = quote.getString("quoteText");
        String quoteAuthor = quote.getString("quoteAuthor");

        if (quoteAuthor.isBlank()) quoteAuthor = "Неизвестный автор";

        out.append(quoteText);
        out.deleteCharAt(quoteText.length());
        out.append("»");
        out.append("\n\n");
        out.append(quoteAuthor);

        return out.toString();
    }

}
