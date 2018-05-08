package com.github.awesomelemon;

import javafx.util.Pair;

import java.sql.*;
import java.util.List;

public class RepoPathProvider {
    private Connection connection;

    RepoPathProvider(Connection connection) {
        this.connection = connection;
    }

    public Pair<String, Integer> getNext() {
        try {
            // create a database connection
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            ResultSet resultSet = statement.executeQuery("SELECT id, path from Solution WHERE ProcessedTime ISNULL limit 1");
            if (!resultSet.isBeforeFirst() ) {
                resultSet.close();
                statement.close();
                connection.commit();
                return null;//result set is empty
            }
            resultSet.next();
            int id = resultSet.getInt("id");
            String path = resultSet.getString("path");
            resultSet.close();
            statement.close();
            connection.commit();
            return new Pair<>(path, id);
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
