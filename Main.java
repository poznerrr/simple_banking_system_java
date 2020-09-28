package banking;

public class Main {
    public static String URL;
    public static void main(String[] args) {
        URL = "jdbc:sqlite:" + args[0];
        UI ui = new UI();
        boolean isWorking = true;
        while (isWorking) {
            isWorking = ui.showMenu();
        }
    }
}
