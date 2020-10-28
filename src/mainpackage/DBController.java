package mainpackage;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DBController {
    public static ArrayList<Integer> books = new ArrayList<>();
    public static ArrayList<Integer> links = new ArrayList<>();

    //JDBC url и данные пользователя mySQL
    private static final String URL = "jdbc:mysql://localhost/library?autoReconnect=true&useSSL=false";
    private static final String USER = "root";
    private static final String PASSWORD = "admin";

    //перменные для работы с JDBC
    private static Connection connection;
    private static Statement statement;
    private static ResultSet resultSet;

    //функция для проверки существования книги по ID
    public static boolean isExist(int bookID) {
        try {
            //строки для открытия соединения с БД/получения ResultSet
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM books\n" +
                    "WHERE book_id = " + bookID);
            return resultSet.next();
        } catch (SQLException se) { System.out.println("Exception: " + se); } finally {
            //строки для закрытия соединения с БД
            try { connection.close(); } catch(SQLException se) {System.out.println("Exception: " + se);}
            try { statement.close(); } catch(SQLException se) {System.out.println("Exception: " + se);}
            try { resultSet.close(); } catch(SQLException se) {System.out.println("Exception: " + se);}
        }
        return false;
    }

    public static void closeConnection() {
        try { connection.close(); } catch(SQLException se) {System.out.println("Exception: " + se);}
        try { statement.close(); } catch(SQLException se) {System.out.println("Exception: " + se);}
        try { resultSet.close(); } catch(SQLException se) {System.out.println("Exception: " + se);}
    }

    //Функция для получения всех книг, необходимых для прочтения переданной книги (ID)
    public static void readBook(int bookID, boolean message) {
        try {
            //устанавливаем соединение и отсылаем запрос
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT book_id FROM books\n" +
                    "WHERE book_id = " + bookID);

            //Если такая книга есть реализуем функцию
            if (resultSet.next()) {
                resultSet = statement.executeQuery("SELECT link_book_id FROM links\n" +
                        "WHERE book_id = " + bookID);
                while (resultSet.next()) {
                    if (!links.contains(resultSet.getInt("link_book_id")) && !books.contains(resultSet.getInt("link_book_id")))
                        links.add(resultSet.getInt("link_book_id"));
                }
                while (!links.isEmpty()) {
                    int link0 = links.get(0);
                    links.remove(0);
                    if (!books.contains(link0)) {
                        books.add(link0);
                        readBook(link0, false);
                    }
                }
                if (books.isEmpty() && message)
                    System.out.println("Вы можете прочитать эту книгу");

            //если такой книги нет
            } else {
                System.out.println("Такой книги нет в библиотеке: " + bookID);
            }
        } catch (SQLException se) { System.out.println("Exception: " + se); }
    }

    //Функция для получения ID последней книги
    public static int getBookID() {
        int switchInt = 0;
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM books");
            while (resultSet.next()) { switchInt = resultSet.getInt("book_id"); }
        } catch (SQLException se) { System.out.println("Exception: " + se); } finally {
            try { connection.close(); } catch(SQLException se) {System.out.println("Exception: " + se);}
            try { statement.close(); } catch(SQLException se) {System.out.println("Exception: " + se);}
            try { resultSet.close(); } catch(SQLException se) {System.out.println("Exception: " + se);}
        }
        return switchInt;
    }

    //Функция для отправки запроса с обновлением БД
    public static void sendUpdate(String update) {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            statement = connection.createStatement();
            statement.executeUpdate(update);
        } catch (SQLException se) { System.out.println("Exception: " + se); } finally {
            try { connection.close(); } catch(SQLException se) {System.out.println("Exception: " + se);}
            try { statement.close(); } catch(SQLException se) {System.out.println("Exception: " + se);}
        }
    }

    //Функция для вывода обеих таблиц
    public static void showTables() throws SQLException {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM books");

            //выводим все строки из таблицы по порядку
            System.out.println("Список книг");
            System.out.println("-------------------------------");
            while (resultSet.next()) {
                byte column = (byte) resultSet.getMetaData().getColumnCount();
                System.out.print(" | ");
                for (int i = 1; i <= column; i++) {
                    System.out.print(resultSet.getString(i) + " | ");
                }
                System.out.print("\n");
            }
            System.out.println("-------------------------------\n");

            resultSet = statement.executeQuery("SELECT * FROM links");

            //выводим все строки из таблицы по порядку
            System.out.println("Список ссылок");
            System.out.println("-------------------------------");
            while (resultSet.next()) {
                byte column = (byte) resultSet.getMetaData().getColumnCount();
                System.out.print(" | ");
                for (int i = 1; i <= column; i++) {
                    System.out.print(resultSet.getString(i) + " | ");
                }
                System.out.print("\n");
            }
            System.out.println("-------------------------------\n");

        } catch (SQLException se) { System.out.println("Exception: " + se); } finally {
            try { connection.close(); } catch(SQLException se) {System.out.println("Exception: " + se);}
            try { statement.close(); } catch(SQLException se) {System.out.println("Exception: " + se);}
            try { resultSet.close(); } catch(SQLException se) {System.out.println("Exception: " + se);}
        }
    }

    //Функция для добавления ссылок к книге
    public static void addLinks(int bookID, ArrayList<Integer> links) {
        ArrayList<Integer> createdLinks = new ArrayList<>();
        ArrayList<Integer> createdBooks = new ArrayList<>();
        if (bookID == 0)
            bookID = getBookID();
        try {
            //устанавливаем соединение и отсылаем запрос
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM books");

            //добавляем все книги в ArrayList
            while (resultSet.next()) { createdBooks.add(resultSet.getInt("book_id")); }
            resultSet = statement.executeQuery("SELECT link_book_id FROM links\n" +
                    "WHERE book_id = " + bookID);

            //добавляем все ссылки в ArrayList
            while (resultSet.next()) { createdLinks.add(resultSet.getInt("link_book_id")); }
            for (Integer link : links) {
                readBook(link, false);

                //проверяем, не ведет ли ссылка на родителя
                if (DBController.books.contains(bookID)) {
                    System.out.println("Не удалось добавить одну из ссылок, тк она ведет на книгу-родителя"); }
                //добавляем в таблицу БД
                else {
                    if (!createdLinks.contains(link) && createdBooks.contains(link) && link != bookID) {
                        statement.executeUpdate("INSERT INTO links (book_id, link_book_id)\n" +
                                "VALUES (" + bookID + ", " + link + ")");
                    }
                }
                DBController.books.clear();
            }

        } catch (SQLException se) { System.out.println("Exception: " + se); } finally {
            try { connection.close(); } catch(SQLException se) {System.out.println("Exception: " + se);}
            try { statement.close(); } catch(SQLException se) {System.out.println("Exception: " + se);}
            try { resultSet.close(); } catch(SQLException se) {System.out.println("Exception: " + se);}
        }
    }

    //Функция для удаления ссылок из книги/удаления всей таблицы с ссылками
    //(переключается через булеву переменную)
    public static void deleteLinks(int bookID, ArrayList<Integer> links, boolean deleteAll) {
        try {
            //устанавливаем соединение и отсылаем запрос
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            statement = connection.createStatement();

            //удаляем все упоминания о книге из БД
            if (deleteAll) {
                statement.executeUpdate("DELETE FROM links\n" +
                        "WHERE book_id = " + bookID + " OR link_book_id = " + bookID);
            } else {
                for (Integer link : links) {
                    statement.executeUpdate("DELETE FROM links\n" +
                            "WHERE book_id = " + bookID + "\n" +
                            "AND link_book_id = " + link);
                }
            }

        } catch (SQLException se) { System.out.println("Exception: " + se); } finally {
            try { connection.close(); } catch(SQLException se) {System.out.println("Exception: " + se);}
            try { statement.close(); } catch(SQLException se) {System.out.println("Exception: " + se);}
        }
    }

    //Очистить таблицу книг в БД
    public static void clearBooksTable() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM books");

            if (resultSet.next()) {
                //выключаем безопасный режим
                statement.executeUpdate("SET SQL_SAFE_UPDATES = 0");

                //удаляем все книги и все ссылки
                statement.executeUpdate("DELETE FROM books");
                statement.executeUpdate("DELETE FROM links");

                //включаем безопасный режим
                statement.executeUpdate("SET SQL_SAFE_UPDATES = 1");

            } else { System.out.println("Ваша библиотека пуста. Удаление невозможно"); }
        } catch (SQLException se) { System.out.println("Exception: " + se); } finally {
            try { connection.close(); } catch(SQLException se) {System.out.println("Exception: " + se);}
            try { statement.close(); } catch(SQLException se) {System.out.println("Exception: " + se);}
            try { resultSet.close(); } catch(SQLException se) {System.out.println("Exception: " + se);}
        }
    }

    //сортировка
    public static void sort() {
        try {
            //строки для открытия соединения с БД/получения ResultSet
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            statement = connection.createStatement();

            //получаем id последней книги
            resultSet = statement.executeQuery("SELECT max(book_id) AS book_id FROM books\n");
            resultSet.next();

            //создаем граф
            Graph graph = new Graph(resultSet.getInt("book_id") + 1);
            resultSet = statement.executeQuery("SELECT books.book_id, links.link_book_id FROM books\n" +
                    "LEFT JOIN links\n" +
                    "ON books.book_id = links.book_id");

            //заполняем граф ребрами, используя данные из БД
            while (resultSet.next()) {
                graph.addEdge(resultSet.getInt("book_id"), resultSet.getInt("link_book_id")); }
            graph.topologicalSort();
        } catch (SQLException se) { System.out.println("Exception: " + se); } finally {
            //строки для закрытия соединения с БД
            try { connection.close(); } catch(SQLException se) {System.out.println("Exception: " + se);}
            try { statement.close(); } catch(SQLException se) {System.out.println("Exception: " + se);}
            try { resultSet.close(); } catch(SQLException se) {System.out.println("Exception: " + se);}
        }
    }
}
