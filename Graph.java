package mainpackage;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Stack;

public class Graph {
    //номер и ребра
    private int v;
    LinkedList<Integer>[] links;

    //конструктор класса Graph
    public Graph(int v) {
        this.v = v;
        links = new LinkedList[v];
        for (int i = 0; i < v; i++)
            links[i] = new LinkedList();
    }

    //метод добавления ребра
    public void addEdge (int v, int w) { links[v].add(w);}

    //метод, вызываемый функцией topologicalSort()
    public void topologicalSortUtil(int v, boolean[] visited, Stack stack) {
        //переменная для проверки, нужно ли добавить текущий № (соответствует ID в БД) в стэк
        boolean add = false;

        //помечаем как посещенный
        visited[v] = true;

        //проверяем все ссылки
        for (Integer i : links[v]) {

            //если есть хотя бы 1 ссылка - добавляем текущий номер в стэк (такой ID существует)
            add = true;

            //если ссылка не посещена - вызываем рекурсию
            if (!visited[i])
                topologicalSortUtil(i, visited, stack);
        }
        if (!stack.contains(v) && add)
            stack.push(v);
    }

    //функция сортировки
    public void topologicalSort() {
        //создаем стэк для хранения списка книг
        Stack stack = new Stack();

        //массив булевых переменных для проверки посещений итератором
        boolean[] visited = new boolean[v];

        //если еще не посещена - вызываем topologicalSortUtil(номер, посещен ли, весь стек)
        for (int i = 0; i < v; i++) {
            if (!visited[i])
                topologicalSortUtil(i, visited, stack);
        }

        //переворачиваем стек для правильного порядка и выводим на экран
        Collections.reverse(stack);
        System.out.println("Вся библиотека в порядке возможного прочтения: ");
        while (!stack.empty())
            System.out.print(stack.pop() + " ");
        System.out.println();
    }
}
