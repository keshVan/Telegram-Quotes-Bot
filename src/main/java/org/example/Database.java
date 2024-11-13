package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private final static String URL = "jdbc:sqlite:favoritesQuotes.db";
    private Connection connection;

    public Database() throws SQLException {
        this.connection = DriverManager.getConnection(URL);
        System.out.println("connected");
    }

    public void addFavoriteQuote(long userId, String quote, String author) {
        String query = "INSERT INTO favorites (user_id, quote, author) VALUES (?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, userId);
            statement.setString(2, quote);
            statement.setString(3, author);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getFavoriteQuotes(long userId) {
        String query = "SELECT quote FROM favorites WHERE userId = ?";
        List<String> res = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, userId);
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                res.add(resultSet.getString("quote") + "\n" + resultSet.getString("author"));
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        return res;
    }
}
