package ru.kornilaev.database;

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

    public void addUser(long userId) {
        String query = "INSERT OR IGNORE INTO users (user_id, curr_index) VALUES (?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, userId);
            statement.setInt(2, 0);
            statement.execute();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addFavoriteQuote(long userId, String quote, String author) {
        String query = "INSERT OR IGNORE INTO favorites (user_id, quote, author) VALUES (?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, userId);
            statement.setString(2, quote);
            statement.setString(3, author);
            statement.execute();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getFavoriteQuotes(long userId) {
        String query = "SELECT * FROM favorites WHERE user_id = ?";
        List<String> res = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, userId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                res.add(resultSet.getString("quote") + "\n\n" + resultSet.getString("author"));
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    public int getFavoriteQuotesCount(long userId) {
        String query = "SELECT COUNT (*) FROM favorites where user_id = ?";
        int res = 0;
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, userId);
            res = statement.executeQuery().getInt(1);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    public String getFavoriteQuote(long userId, int index) {
        String query = "SELECT * FROM favorites WHERE user_id = ? ORDER BY id LIMIT 1 OFFSET ?";
        String res = "";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, userId);
            statement.setInt(2, index);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.getString("quote") + "\n\n" + resultSet.getString("author");
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    public List<Long> getUsersId() {
        String query = "SELECT user_id FROM users";
        List<Long> res = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                res.add(resultSet.getLong(1));
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    public void removeFromFavorites(long userId, String quote, String author) {
        String query = "DELETE FROM favorites WHERE user_id = ? AND quote = ? AND author = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, userId);
            statement.setString(2, quote);
            statement.setString(3, author);
            statement.execute();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int currentIndex(long userId) {
        String query = "SELECT curr_index FROM users WHERE user_id = ?";
        int res = 0;
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, userId);
            ResultSet resultSet = statement.executeQuery();
            res = resultSet.getInt("curr_index");
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    public void updateIndex(long userId, int newIndex) {
        String query =  "UPDATE users SET curr_index = ? WHERE user_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, newIndex);
            statement.setLong(2, userId);
            statement.execute();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
