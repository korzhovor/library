package mainpackage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Controller {
    //вывод опций на экран и выбор
    public static boolean showOptions() {
        System.out.println("1.Прочитать книгу");
        System.out.println("2.Вывести таблицы");
        System.out.println("3.Добавить книгу");
        System.out.println("4.Удалить книгу");
        System.out.println("5.Редактировать книгу");
        System.out.println("6.Очистить библиотеку");
        System.out.println("7.Сортировать все");
        System.out.println("0.Выход");

        //переход к выбранной опции
        byte choose;
        try {

            //readInt и readIntMenu - функции, созданные для удобства и проверки ввода
            do { choose = (byte) readIntMenu("[0-7]"); } while (choose == -1);
            switch (choose) {
                case 1 -> {
                    System.out.println("Введите id книги");
                    int bookID;

                    //считываем id пока не получим правильную строку
                    do { bookID = readInt(); } while (bookID == -1);

                    //вызываем метод, выводящий все дерево книг по переданному ID
                    DBController.readBook(bookID, true);

                    //переворачиваем ArrayList в классе DBController
                    Collections.reverse(DBController.books);

                    //если он не пуст, выводим и чистим
                    if (!DBController.books.isEmpty()) {
                        System.out.println("Список книг, обязательных к прочтению:");
                        for (Integer readBook : DBController.books) {
                            System.out.println(readBook);
                        }
                        DBController.books.clear();
                        DBController.closeConnection();
                    }
                }

                //функция для показа всех таблиц
                case 2 -> DBController.showTables();

                //функция для добавления книг
                case 3 -> addBook();

                //функция для удаления
                case 4 -> deleteBook();

                //функция для редактирования книг и ссылок
                case 5 -> editBook();

                //очистить БД
                case 6 -> DBController.clearBooksTable();

                //функция сортировки sort() (выводим все книги в порядке, в котором их возможно прочитать)
                case 7 -> DBController.sort();

                //возвращаем false в Main. Выход из программы
                case 0 -> {
                    return false;
                }
            }
        } catch ( SQLException e) {
            e.printStackTrace();
        }

        //если все прошло успешно возвращаем true. Продолжаем работу
        return true;
    }

    //создание книги
    private static void addBook() {
        //обьявляем переменные
        ArrayList<Integer> links = new ArrayList<>();
        String bookName, bookAuthor;
        System.out.println("Введите название книги");

        //читаем, пока пользователь не заполнит все верно
        do {
            bookName = readString();
            if (bookName.equals(""))
                System.out.println("Название книги не может быть пустым");
        } while (bookName.equals(""));
        System.out.println("Введите автора книги");
        bookAuthor = String.valueOf(readString());

        //отправляем готовый запрос классу-контроллеру (КК)
        DBController.sendUpdate("INSERT INTO books (name, author)\n" +
                "VALUES (\"" + bookName + "\", \"" + bookAuthor + "\")");

        System.out.println("Введите ссылки");

        //считываем ссылки, пока не будет введена пустая строка (addInt вернет -1)
        while (true) {
            String addInt = "" + readInt();
            if (!addInt.equals("") && !addInt.equals("-1"))
                links.add(Integer.parseInt(addInt));
            else
                break;
        }

        //добавляем ссылки
        DBController.addLinks(0, links);
    }

    //удаление книги
    public static void deleteBook() {
        //обьявляем переменные и считываем их
        int bookID;
        System.out.println("Введите id книги");
        do {
            bookID = readInt();
        } while (bookID == -1);

        //отправляем готовыe запросы классу-контроллеру
        DBController.sendUpdate("DELETE FROM books\n" +
                "WHERE book_id = " + bookID);

        DBController.deleteLinks(bookID, new ArrayList<>(), true);
    }

    //меню с выборами вариантов редактирования книги и их реализацией
    private static boolean editBook() {
        //обьявляем переменные и считываем их
        byte choose;
        int bookID;
        System.out.println("Введите id книги");

        //читаем, пока не получим верную вводную информацию
        do { bookID = readInt(); } while (bookID == -1);
        if (!DBController.isExist(bookID)) {
            System.out.println("Такой книги не существует");
            return false;
        }
        //выводим опции на экран
        System.out.println("1. Изменить название");
        System.out.println("2. Изменить автора");
        System.out.println("3. Добавить ссылки");
        System.out.println("4. Удалить ссылки");
        System.out.println("0. Назад");

        //снова реализуем выбор через переменную, отправляем соответствующий запрок классу-контроллеру
        choose = (byte) readIntMenu("[0-4]");
        switch (choose) {
            case 1 -> {
                String name;
                System.out.println("Введите новое название");
                do {
                    name = readString();
                    if (name.equals(""))
                        System.out.println("Название не может быть пустым");
                } while (name.equals(""));
                DBController.sendUpdate("UPDATE books\n" +
                        "SET name = \"" + name + "\"\n" +
                        "WHERE book_id = " + bookID);
            }
            case 2 -> {
                System.out.println("Введите нового автора");
                DBController.sendUpdate("UPDATE books\n" +
                        "SET author = \"" + readString() + "\"\n" +
                        "WHERE book_id = " + bookID);
            }
            case 3 -> {
                System.out.println("Введите id книг-ссылок для добавления");
                ArrayList<Integer> links = new ArrayList<>();
                while (true) {
                    String addInt = "" + readInt();
                    if (!addInt.equals("0"))
                        links.add(Integer.parseInt(addInt));
                    else
                        break;
                }
                DBController.addLinks(bookID, links);
            }
            case 4 -> {
                System.out.println("Введите id книг-ссылок для удаления");
                ArrayList<Integer> links = new ArrayList<>();
                while (true) {
                    String addInt = "" + readInt();
                    if (!addInt.equals(""))
                        links.add(Integer.parseInt(addInt));
                    else
                        break;
                }
                DBController.deleteLinks(bookID, links, false);
            }
        }
        return true;
    }

    //функция для чтения 1-значного int с параметром line
    private static int readIntMenu(String line) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        int result = -1;
        try {
            String string = reader.readLine();
            Pattern pattern = Pattern.compile(line);
            Matcher matcher = pattern.matcher(string);
            if (matcher.find() && string.length() == 1) {
                result = Integer.parseInt(string);
            } else {
                System.out.println("Недопустимое значение");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    //функция для считывания любого int
    private static int readInt() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            String string = reader.readLine();
            Pattern pattern = Pattern.compile("[^0-9]");
            Matcher matcher = pattern.matcher(string);
            if (string.equals(""))
                return 0;
            if (matcher.find()) {
                System.out.println("Недопустимое значение");
            } else {
                return Integer.parseInt(string);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    //функция для считывания строк
    public static String readString() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            return reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
