import java.util.*;

public class GameSession {

    public static final int FIELD_EMPTY = 0;
    public static final int FIELD_X = 10;
    public static final int FIELD_O = 100;

    private final String sessionID;
    private final Date createdDate;
    int[][] field;
    boolean xTurn;
    boolean gameOver;
    private ClientHandler playerX;
    private ClientHandler playerO;

    public GameSession(){
        this.sessionID = generateSessionID();
        this.field = new int [3][3];
        this.createdDate = new Date();
        initGame();
    }

    String generateSessionID(){
        return "SESSION_" + System.currentTimeMillis() + "_" + new Random().nextInt(1000);
    }
    public Date getCreatedDate() {return createdDate;}

    public void initGame(){
        for (int i = 0; i<3; i++){
            for (int j = 0; j<3; j++){
                field[i][j] = FIELD_EMPTY;
            }
        }
        xTurn = true;
        gameOver = false;
    }
    public synchronized boolean addPlayer(ClientHandler client) {
        if (playerX == null) {
            playerX = client;
            client.setPlayerSymbol("X"); // УСТАНАВЛИВАЕМ СИМВОЛ
            System.out.println("Player X joined session: " + sessionID);
            return true;
        } else if (playerO == null) {
            playerO = client;
            client.setPlayerSymbol("O"); // УСТАНАВЛИВАЕМ СИМВОЛ
            System.out.println("Player O joined session: " + sessionID);
            return true;
        }
        return false;
    }

    public boolean isFull(){
        return playerX != null && playerO != null;
    }

    public boolean isEmpty(){
        return playerX == null && playerO == null;
    }

    public synchronized void removePlayer(ClientHandler client) {
        if (client == playerX) {
            playerX = null;
            if (playerO != null) {
                playerO.sendMessage("ERROR:Opponent disconnected");
            }
            System.out.println("Player X left session: " + sessionID);
            if (client == playerO) {
                playerO = null;
            } else if (playerX != null) {
                playerX.sendMessage("ERROR:Opponent disconnected");
            }
            System.out.println("Player O left session: " + sessionID);
        }

    }

    public void restartGame(){
        if(playerX != null && playerO != null && playerX.isConnected() && playerO.isConnected()){
            initGame();
            broadcastGameState();
            System.out.println("Game restarted in session: " + sessionID);
        }
    }

    private int checkState(){
        for(int i = 0; i<3; i++) {
            int rawSum = field[i][0] + field[i][1] + field[i][2];
            int culSum = field[0][i] + field[1][i] + field[2][i];
            if (rawSum == FIELD_X * 3 || rawSum == FIELD_O * 3) return rawSum;
            if (culSum == FIELD_X * 3 || culSum == FIELD_O * 3) return culSum;
        }

        int digL = field[0][0]+field[1][1]+field[2][2];
        int digR = field[0][2]+field[1][1]+field[2][0];
        if (digL == FIELD_X*3||digL == FIELD_O*3) return digL;
        if (digR == FIELD_X*3||digR == FIELD_O*3) return digR;

        boolean hasEmpty = false;
        for (int i = 0; i<3; i++){
            for (int j =0; j<3; j++){
                if(field[i][j] == FIELD_EMPTY){
                    hasEmpty = true;
                    break;
                }

            }
            if (hasEmpty) break;
        }
        return hasEmpty?0:-1;

    }

    public String getSessionId() {
        return sessionID;
    }

    public synchronized void makeMove(ClientHandler client, int i, int j) {
        if (gameOver) {
            client.sendMessage("ERROR:Game is over");
            return;
        }

        if ((xTurn && client != playerX)||(!xTurn && client != playerO)){
            client.sendMessage("ERROR:Not your turn");
            return;
        }

        field[i][j] = xTurn? FIELD_X:FIELD_O;
        xTurn = !xTurn;

        int result = checkState();
        if(result!=0){
            gameOver = true;
            if (result == FIELD_X*3) broadcastWinner("X");
            if (result == FIELD_O*3) broadcastWinner("O");
            else broadcastWinner("DRAW");
        }
        else broadcastGameState();

    }
    private String getBoardState(){
        StringBuilder board = new StringBuilder();
        for(int i = 0; i<3; i++){
            for(int j = 0; j<3; j++){
                board.append(field[i][j]);
                if(i!=2||j!=2)board.append(',');
            }
        }
        return board.toString();
    }
    public void broadcastGameState(){
        String boardState = getBoardState();
        String turnInfo = "TURN:"+(xTurn? "X":"O");

        if(playerX != null && playerX.isConnected()){
            playerX.sendMessage("BOARD:" + boardState);
            playerX.sendMessage(turnInfo);
        }
        if(playerO != null && playerO.isConnected()){
            playerO.sendMessage("BOARD:" + boardState);
            playerO.sendMessage(turnInfo);
        }
    }
    private void broadcastWinner(String winner) {
        if (playerX != null && playerX.isConnected()) {
            playerX.sendMessage("WINNER:" + winner);
        }
        if (playerO != null && playerO.isConnected()) {
            playerO.sendMessage("WINNER:" + winner);
        }

        System.out.println("Game finished in session " + sessionID + ". Winner: " + winner);
    }
    public boolean hasPlayer(ClientHandler client) {
        return client == playerX || client == playerO;
    }
}
