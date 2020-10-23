package mainpackage;

public class Main {

    public static void main(String[] args) {
        //переменная для работы программы и цикл работы
        boolean isWorking = true;

        while (isWorking) {
            isWorking = Controller.showOptions();
        }
    }
}
