/**
 * Created by Lídia on 29/08/2018
 */

package ManagementServer.gameservercommunication;

import CommunicationCommons.Heartbeat;
import CommunicationCommons.HeartbeatInterface;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class HeartbeatService extends UnicastRemoteObject implements HeartbeatInterface {

    private String databaseIp;
    private String gameServer;
    private int count;

    public HeartbeatService(String databaseIp) throws RemoteException {
        this.databaseIp = databaseIp;
        this.gameServer = null;
        this.count = 0;
    }

    public synchronized String getGameServer() {
        return gameServer;
    }

    public synchronized void setGameServer(String gameServer) {
        this.gameServer = gameServer;
    }

    public synchronized int getCount() {
        return count;
    }

    public synchronized void setCount(int count) {
        this.count = count;
    }

    public synchronized void resetHeartbeat() {
        if (gameServer != null) {
            System.out.println("(" + getGameServer()
                    + " missed more than " + Heartbeat.MAX_COUNT + " heartbeats).");
        }
        setCount(0);
        setGameServer(null);
    }

    @Override
    public String heartbeat(String gameServer) throws RemoteException {
        if (getGameServer() == null || getCount() > Heartbeat.MAX_COUNT) {
            System.out.println("==> New game server connected: " + gameServer + " <==");
            setCount(0);
            setGameServer(gameServer);
        } else if (getGameServer().equals(gameServer)) {
            setCount(0);
//            System.out.println("-- Heartbeat received for game server " + gameServer + " --");
        }

        return databaseIp;
    }
}
