import java.util.*;
import java.util.concurrent.*;

public class GameSessionManager {
    private final Map<String, GameSession> activeSessions;

    public GameSessionManager(){
        this.activeSessions = new ConcurrentHashMap<>();
    }

    public synchronized GameSession addPlayerToGame(ClientHandler client){

        for(GameSession session: activeSessions.values()){
            if (session.isFull() && session.hasPlayer(client)) return session;
            if (!session.isFull()){
                if(session.addPlayer(client))return session;
            }
        }
        int maxSessions = 100;
        if (activeSessions.size() < maxSessions){
            GameSession newSession = new GameSession();
            newSession.addPlayer(client);
            activeSessions.put(newSession.getSessionId(), newSession);
            System.out.println("Created new game session: " + newSession.getSessionId());
            System.out.println("Active sessions: " + activeSessions.size());
            return  newSession;
        }
        System.out.println("Cannot create new session: maximum sessions reached");
        return null;
    }
    public synchronized void removeSession(String sessionID){
        GameSession session = activeSessions.remove(sessionID);
        if (session != null){
            System.out.println("Removed game session: " + sessionID);
            System.out.println("Active sessions: " + activeSessions.size());
        }
    }
    public synchronized void cleanEmptySessions(){
        Iterator<Map.Entry<String, GameSession>> iterator = activeSessions.entrySet().iterator();
        int removedCount = 0;

        while(iterator.hasNext()){
            Map.Entry<String, GameSession> entry = iterator.next();
            if(entry.getValue().isEmpty()){
                iterator.remove();
                removedCount++;
            }
        }
        if (removedCount > 0){
            System.out.println("Cleaned up " + removedCount + " empty sessions");
        }
    }
    public int getActiveSessionsCount(){return activeSessions.size();}

    public void printSessionStatus(){
        System.out.println("=== Game Session Statistics ===");
        System.out.println("Active sessions: " + activeSessions.size());
        for (GameSession session : activeSessions.values()) {
            System.out.println("Session: " + session.getSessionId() +
                    ", Created: " + session.getCreatedDate());
        }
    }


}

