import java.io.FileReader;
import java.sql.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Main {
    public static void main(String[] args) {
        DatabaseConnectionFirdavs dbConnection = DatabaseConnectionFirdavs.getInstance();

        int rowsInserted = dbConnection.insertQuery("INSERT INTO student (Name, Surname, Gender, Age, Major, Faculty) VALUES ('John', 'Doe', 'Male', 20, 'Computer Science', 'Engineering')");
        System.out.println("Rows inserted by insertQuery: " + rowsInserted);
        System.out.println();

        System.out.println("Query #1: SELECT * FROM student");
        ResultSet resultSet = dbConnection.executeQuery("SELECT * FROM student");
        printResultSet(resultSet);
        System.out.println();

        System.out.println("Query #2: SELECT * FROM student WHERE age = (SELECT MAX(age) FROM student)");
        ResultSet resultSet1 = dbConnection.executeQuery("SELECT * FROM student WHERE age = (SELECT MAX(age) FROM student)");
        printResultSet(resultSet1);
        System.out.println();

        int rowsAffected = dbConnection.updateQuery("UPDATE student SET age = 25 WHERE name = 'David' AND surname = 'Lee'");
        System.out.println("Rows affected by updateQuery: " + rowsAffected);
        System.out.println();

        System.out.println("Database connection URL: " + dbConnection.getUrl());
        System.out.println("Database connection username: " + dbConnection.getUsername());
    }

    private static void printResultSet(ResultSet resultSet) {
        try {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (resultSet.next()) {
                StringBuilder row = new StringBuilder();
                for (int i = 1; i <= columnCount; i++) {
                    row.append(resultSet.getString(i));
                    if (i < columnCount) {
                        row.append(" ");
                    }
                }
                System.out.println(row.toString());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}

class DatabaseConnectionFirdavs {
    private String url;
    private String username;
    private String password;

    private DatabaseConnectionFirdavs() {
        loadConfigFromFile();
    }

    private static class Holder {
        private static final DatabaseConnectionFirdavs INSTANCE = new DatabaseConnectionFirdavs();
    }

    public static DatabaseConnectionFirdavs getInstance() {
        return Holder.INSTANCE;
    }

    private void loadConfigFromFile() {
        try {
            JsonParser parser = new JsonParser();
            JsonObject config = parser.parse(new FileReader("src/database_config.json")).getAsJsonObject();

            url = config.get("url").getAsString();
            username = config.get("username").getAsString();
            password = config.get("password").getAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ResultSet executeQuery(String query) {
        ResultSet resultSet = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection connection = DriverManager.getConnection(url, username, password);
            Statement statement = connection.createStatement();
            resultSet = statement.executeQuery(query);
        } catch (Exception e) {
            System.out.println(e);
        }
        return resultSet;
    }

    public int updateQuery(String query) {
        int rowsAffected = 0;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection connection = DriverManager.getConnection(url, username, password);
            Statement statement = connection.createStatement();
            rowsAffected = statement.executeUpdate(query);
            connection.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        return rowsAffected;
    }

    public int insertQuery(String query) {
        int rowsInserted = 0;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection connection = DriverManager.getConnection(url, username, password);
            Statement statement = connection.createStatement();

            ResultSet resultSet = statement.executeQuery("SELECT MAX(ID) FROM student");
            int lastID = 0;
            if (resultSet.next()) {
                lastID = resultSet.getInt(1);
            }

            rowsInserted = statement.executeUpdate("INSERT INTO student (ID, Name, Surname, Gender, Age, Major, Faculty) VALUES (" + (lastID + 1) + ", 'John', 'Doe', 'Male', 20, 'Computer Science', 'Engineering')");
            connection.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        return rowsInserted;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }
}
