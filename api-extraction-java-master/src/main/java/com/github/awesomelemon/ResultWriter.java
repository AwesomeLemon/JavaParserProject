package com.github.awesomelemon;

import java.sql.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ResultWriter {
    private Connection connection;
    private static ExecutorService executor = Executors.newSingleThreadExecutor();


    ResultWriter(Connection connection) {
        this.connection = connection;
    }

    public void write(List<Method> methods, int repoId) {
        executor.submit(() -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "insert into Method(name, comment, calls, solution_id) values(?, ?, ?, " + repoId + ")");
                preparedStatement.setQueryTimeout(30);
                for (Method method : methods) {
                    preparedStatement.setString(1, method.getName());
                    preparedStatement.setString(2, method.getJavadocComment());
                    preparedStatement.setString(3, method.getCallSequence());
                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
                preparedStatement.close();
                connection.commit();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    void markSolutionProcessed(int repoId) {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate("update Solution set ProcessedTime=CURRENT_TIMESTAMP where id = " + repoId);
            statement.close();
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createMethodTable() {
        try {
            // create a database connection
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            statement.executeUpdate("CREATE TABLE Method (id INTEGER PRIMARY KEY, name VARCHAR, comment VARCHAR, calls VARCHAR, repo_id INTEGER)");
            statement.close();
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
