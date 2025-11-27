//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8888;

        System.out.println("Запуск сервера TicTacToe на порту " + port);
        System.out.println("Сервер готов принимать подключения...");
        System.out.println("Для остановки сервера нажмите Ctrl+C");

        TicTakServer server = new TicTakServer(port);
        server.start();
    }

}
