package mainpackage;

public class Main {

    public static void main(String[] args) {
        //переменная для работы программы
        boolean isWorking = true;

        //вызываем функцию показа меню, пока она не вернет false
        while (isWorking) {
            isWorking = Controller.showOptions();
        }
    }
}
