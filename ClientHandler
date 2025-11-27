import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable{
    private final Socket socket;
    private final TicTakServer server;
    private PrintWriter out;
    private BufferedReader in;
    private GameSession game;
    private String playerSymbol;
    private boolean connected = true;

    public ClientHandler(Socket socket, TicTakServer server){
        this.socket = socket;
        this.server = server;
        initializeStreams();
    }
    private void initializeStreams(){
        try{
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }catch (IOException e){
            System.err.println("Error initializing streams for client: " + e.getMessage());
            connected = false;
        }
    }
    public void run(){
        try {
            game = server.getGameManager().addPlayerToGame(this);
            if (game == null){
                sendMessage("ERROR:Server is full or unavailable");
                return;
            }
            sendMessage(playerSymbol);
            game.broadcastGameState();

            String message;
            while((message = in.readLine())!= null && connected){
                processClientMessage(message);
            }
        }
        catch (IOException e){
            System.out.println("Client disconnected" + e.getMessage());
        }
        finally {
            disconnect();
        }
    }
    public void disconnect(){
        connected = false;
        try {
            if (out != null) out.close();
            if (socket!=null)socket.close();
            if (in != null) in.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (game != null){
            game.removePlayer(this);
        }
        server.removeClient(this);
    }

    private void handleMoveMessage(String message){
        if (game == null){
            sendMessage("ERROR:No active game");
            return;
        }
        try{
            String moveData = message.substring(5);
            String[] cords = moveData.split(",");

            // Проверка корректности количества координат
            if (cords.length != 2) {
                sendMessage("ERROR:Invalid coordinates format. Expected: MOVE:x,y");
                return;
            }

            int i = Integer.parseInt(cords[0]);
            int j = Integer.parseInt(cords[1]);
            game.makeMove(this, i, j);

        } catch (NumberFormatException e) {
            sendMessage("ERROR:Coordinates must be numbers");
        } catch (ArrayIndexOutOfBoundsException e) {
            sendMessage("ERROR:Invalid coordinates format");
        } catch (Exception e) {
            sendMessage("ERROR:Invalid move - " + e.getMessage());
        }
    }
    private void handleRestartMessage(){
        if (game != null){
            game.restartGame();
        }
    }


    public void sendMessage(String message){
        if(out != null && connected) out.println(message);
    }
    private void processClientMessage(String message){
        System.out.println("Message from " + message);

        if (message.startsWith("MOVE:")) handleMoveMessage(message);
        else if (message.startsWith("RESTART:")) handleRestartMessage();
        else if (message.startsWith("STATUS:")) sendMessage("STATUS:Connected to server. Game: " + (game != null ? "active" : "waiting"));
    }

    public void setPlayerSymbol(String symbol) {
        this.playerSymbol = symbol;
    }


    public String getPlayerSymbol(String playerSymbol){
        return playerSymbol;
    }

    public boolean isConnected() {
        return connected && !socket.isConnected();
    }
}
