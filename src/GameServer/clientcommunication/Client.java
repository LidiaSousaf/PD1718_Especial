/**
 * Created by Lídia on 30/08/2018
 */

package GameServer.clientcommunication;

import DatabaseCommunication.models.DbPlayer;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client {
    private DbPlayer dbPlayer;
    private int currentPairId;
    private Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;

    public Client(DbPlayer dbPlayer, int currentPairId, Socket socket, ObjectInputStream ois, ObjectOutputStream oos){
        this.dbPlayer = dbPlayer;
        this.currentPairId = currentPairId;
        this.socket = socket;
        this.ois = ois;
        this.oos = oos;
    }

    public DbPlayer getDbPlayer() {
        return dbPlayer;
    }

    public int getCurrentPairId() {
        return currentPairId;
    }

    public Socket getSocket() {
        return socket;
    }

    public ObjectInputStream getOis() {
        return ois;
    }

    public ObjectOutputStream getOos() {
        return oos;
    }
}
