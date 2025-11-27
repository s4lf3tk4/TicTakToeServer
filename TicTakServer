import java.io.*;
import java.net.*;
import java.util.*;

public class TicTakServer {
    private final int port;
    private ServerSocket serverSocket;
    private final Set<ClientHandler> clients = Collections.synchronizedSet(new HashSet<>());
    private final GameSessionManager gameManager;

    public TicTakServer(int port) {
        this.port = port;
        this.gameManager = new GameSessionManager();
    }

    public void start() {
        try {
            // Используем 0.0.0.0 для приема подключений со всех интерфейсов
            serverSocket = new ServerSocket(port, 50, InetAddress.getByName("0.0.0.0"));
            System.out.println("✅ Server started successfully on port " + port);
            System.out.println("✅ Waiting for connections...");

            printEnhancedServerInfo();

            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                String clientAddress = socket.getInetAddress().getHostAddress();
                System.out.println("🔗 New client connected from: " + clientAddress);

                // Проверяем не локальное ли подключение
                if (socket.getInetAddress().isSiteLocalAddress()) {
                    System.out.println("   📍 Local connection");
                } else {
                    System.out.println("   🌐 Remote connection");
                }

                ClientHandler client = new ClientHandler(socket, this);
                clients.add(client);
                new Thread(client).start();

                System.out.println("📊 Total connected clients: " + clients.size());
            }
        } catch (IOException e) {
            System.err.println("❌ Server error: " + e.getMessage());
        } finally {
            stop();
        }
    }

    private void printEnhancedServerInfo() {
        try {
            System.out.println("\n🌐 NETWORK CONFIGURATION:");
            System.out.println("=================================");

            // Локальные адреса
            System.out.println("📍 Local addresses:");
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isUp() && !networkInterface.isLoopback()) {
                    Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress address = addresses.nextElement();
                        if (address instanceof Inet4Address) {
                            System.out.println("   - " + address.getHostAddress() + ":" + port);
                        }
                    }
                }
            }

            // Попытка получить внешний IP (через публичный сервис)
            System.out.println("\n🌍 For remote connections:");
            System.out.println("   You need to:");
            System.out.println("   1. Configure PORT FORWARDING on your router");
            System.out.println("   2. Forward port " + port + " to this computer");
            System.out.println("   3. Use your PUBLIC IP address");

            // Автоматическое определение публичного IP
            getPublicIP();

        } catch (SocketException e) {
            System.err.println("Error getting network info: " + e.getMessage());
        }
    }

    private void getPublicIP() {
        try {
            URL whatismyip = new URL("http://checkip.amazonaws.com");
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    whatismyip.openStream()));
            String publicIp = in.readLine();
            System.out.println("   📍 Your public IP: " + publicIp + ":" + port);
            in.close();
        } catch (Exception e) {
            System.out.println("   ⚠️  Could not determine public IP automatically");
            System.out.println("   💡 Find your public IP at: https://whatismyipaddress.com/");
        }
    }

    public GameSessionManager getGameManager() {
        return gameManager;
    }

    public void stop() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }

            synchronized (clients) {
                System.out.println("Disconnecting " + clients.size() + " clients...");
                for (ClientHandler client : clients) {
                    client.disconnect();
                }
                clients.clear();
            }

            System.out.println("✅ Server stopped gracefully");
        } catch (IOException e) {
            System.err.println("❌ Error stopping server: " + e.getMessage());
        }
    }

    public void removeClient(ClientHandler client) {
        boolean removed = clients.remove(client);
        if (removed) {
            System.out.println("Client disconnected. Total clients: " + clients.size());
        }
    }

    public int getConnectedClientsCount() {
        return clients.size();
    }

    // Добавляем обработку сигналов завершения
    public void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n🛑 Shutdown signal received...");
            stop();
        }));
    }

    public static void main(String[] args) {
        int port = 8888;

        // Чтение порта из аргументов
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number, using default: 8888");
            }
        }

        TicTakServer server = new TicTakServer(port);
        server.addShutdownHook();
        server.start();
    }
}
