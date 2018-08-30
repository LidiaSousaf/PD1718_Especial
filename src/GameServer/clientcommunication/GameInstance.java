/**
 * Created by Lídia on 30/08/2018
 */

package GameServer.clientcommunication;

import DatabaseCommunication.DatabaseCommunication;

import java.util.Observable;
import java.util.Observer;

public class GameInstance implements Runnable, Observer {

    private Client[] players;
    private DatabaseCommunication database;

    public GameInstance(Client[] players, DatabaseCommunication database) {
        this.players = players;
        this.database = database;
    }

    @Override
    public void run() {

    }

    @Override
    public void update(Observable o, Object arg) {

    }
}
