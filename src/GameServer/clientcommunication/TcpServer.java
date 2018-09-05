/**
 * Created by Lídia on 30/08/2018
 */

package GameServer.clientcommunication;

import CommunicationCommons.GameCommConstants;
import CommunicationCommons.PlayerLogin;
import CommunicationCommons.remoteexceptions.NotLoggedException;
import CommunicationCommons.remoteexceptions.PairTimedOutException;
import DatabaseCommunication.DatabaseCommunication;
import DatabaseCommunication.exceptions.PairNotFoundException;
import DatabaseCommunication.exceptions.PlayerNotFoundException;
import DatabaseCommunication.models.DbPair;
import DatabaseCommunication.models.DbPlayer;
import GameServer.GameServer;
import GameServer.exceptions.ClientNotFoundException;
import GameServer.exceptions.InvalidGameLoginException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TcpServer implements Runnable {

    private static final String DATABASE_NAME = "three_in_row";
    private static final String DATABASE_USER_NAME = "GameServer";
    private static final String DATABASE_PASSWORD = "ThreeInRow_1718";

    private List<Client> clientList;
    private ServerSocket serverSocket;
    private DatabaseCommunication database;

    public TcpServer(String databaseIp) {
        clientList = new ArrayList<>();
        this.database = new DatabaseCommunication(DATABASE_NAME,
                DATABASE_USER_NAME, DATABASE_PASSWORD, databaseIp);
        try {
            serverSocket = new ServerSocket(GameCommConstants.TCP_PORT);
            serverSocket.setSoTimeout(GameCommConstants.SOCKET_TIMEOUT);
        } catch (IOException e) {
            System.err.println("Error: Failed to create server socket: " + e);
            GameServer.stopThreads = true;
        }
    }

    private DbPlayer verifyPlayerLogin(PlayerLogin login) throws InvalidGameLoginException, PlayerNotFoundException {
        DbPlayer player = database.getPlayerByUserName(login.getUserName());
        if (player.getUserName().equals(login.getUserName())
                && player.getPassword().equals(login.getPassword())
                && player.getIpAddress().equals(login.getIpAddress())
                && player.isLogged()) {
            return player;
        } else {
            throw new InvalidGameLoginException();
        }
    }

    private int getCurrentPairId(DbPlayer player) throws PairNotFoundException {
        DbPair pair = database.getCurrentPairForPlayer(player);

        if (player.getId() == pair.getPlayer1Id()) {
            return pair.getPlayer2Id();
        } else {
            return pair.getPlayer1Id();
        }
    }

    private void addClientToList(Client newClient) {
        clientList.add(newClient);

        try {
            //if the pair for the current client is already connected,
            //start the new game instance
            int pairIndex = getClientIndex(newClient.getCurrentPairId());
            Client clients[] = new Client[2];
            clients[0] = clientList.get(pairIndex);
            clients[1] = newClient;
            GameInstance gameInstance = new GameInstance(clients, database);
            Thread gameThread = new Thread(gameInstance);
            gameThread.start();

            removeClientFromList(clients[0].getDbPlayer().getId());
            removeClientFromList(clients[1].getDbPlayer().getId());

        } catch (ClientNotFoundException e) {
            //this means that the current client was the first to connect from it's pair
            //no need to do anything
        }

    }

    private int getClientIndex(int playerId) throws ClientNotFoundException {
        for (int i = 0; i < clientList.size(); i++) {
            if (clientList.get(i).getDbPlayer().getId() == playerId) {
                return i;
            }
        }

        throw new ClientNotFoundException();
    }

    private void removeClientFromList(int playerId) {
        Iterator<Client> it = clientList.iterator();

        while (it.hasNext()) {
            Client client = it.next();
            if (client.getDbPlayer().getId() == playerId) {
                it.remove();
            }
        }
    }

    private void incClientsWaitingPeriods() {
        Iterator<Client> it = clientList.iterator();

        while (it.hasNext()){
            Client client = it.next();
            client.incWaitingPeriods();

            //Each client will only wait MAX_TIME_OUTS * 10 seconds for its pair to connect
            //The connection will then be refused
            if (client.getWaitingPeriods() >= GameCommConstants.MAX_TIME_OUTS) {
                try {
                    client.getOos().writeObject(new PairTimedOutException());
                    client.getSocket().close();
                } catch (IOException e) {
//                    e.printStackTrace();
                }

                it.remove();
            }
        }
    }


    private void sendConnectionRefused(Socket cliSocket) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(cliSocket.getOutputStream());
        out.writeObject(new NotLoggedException());
        out.flush();
        cliSocket.close();
    }

    private void closeClientSockets() {
        for (Client client : clientList) {
            try {
                client.getSocket().close();
            } catch (Exception e) {
//                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        if (!database.connect()) {
            System.out.println("Connection to database failed.");
            GameServer.stopThreads = true;
        } else {
            System.out.println("Connection do database established.");
        }

        while (!GameServer.stopThreads) {
            try {
                Socket cliSocket = serverSocket.accept();

                System.out.println("Received connection request from " +
                        cliSocket.getInetAddress().getHostAddress() +
                        ":" + cliSocket.getPort());

                ObjectOutputStream oos = new ObjectOutputStream(cliSocket.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(cliSocket.getInputStream());
                PlayerLogin login = null;
                try {
                    login = (PlayerLogin) ois.readObject();

                    String clientIp = cliSocket.getInetAddress().getHostAddress();
                    login.setIpAddress(clientIp);
                    //verify in database if the user is logged in and if is paired;
                    //if not -> send -1 to the client and continue loop
                    try {
                        DbPlayer dbPlayer = verifyPlayerLogin(login);
                        int currentPairId = getCurrentPairId(dbPlayer);

                        System.out.println("> New client accepted -> userName: " + login.getUserName());

                        Client client = new Client(dbPlayer, currentPairId, cliSocket,
                                ois, oos);

                        client.getOos().writeObject(GameCommConstants.CONNECTION_ACCEPTED);
                        client.getOos().flush();

                        addClientToList(client);

                    } catch (PlayerNotFoundException e) {
                        System.out.println("Client \"" + login.getUserName() + "\" wasn't found in the database.");
                        sendConnectionRefused(cliSocket);
                    } catch (InvalidGameLoginException e) {
                        System.out.println("Client \"" + login.getUserName() + "\" is not authorized to start playing.");
                        sendConnectionRefused(cliSocket);
                    } catch (PairNotFoundException e) {
                        System.out.println("Client \"" + login.getUserName() + "\" is not authorized to start playing. (Not paired)");
                        sendConnectionRefused(cliSocket);
                    }
                } catch (ClassNotFoundException e) {
                    System.err.println("Error receiving client request: " + e);
                    cliSocket.close();
                }
            } catch (SocketTimeoutException e) {
                //Make sure the server doesn't get indefinitely stuck in the loop
                incClientsWaitingPeriods();
            } catch (IOException e) {
                System.err.println("TCP socket error: " + e);
                GameServer.stopThreads = true;
                break;
            }
        }

        closeClientSockets();

        if (serverSocket != null) {
            try {
                serverSocket.close();
                System.out.println("==> ServerSocket closed <==");
            } catch (IOException e) {
                System.err.println("Error closing ServerSocket: " + e);
            }
        }

        if (database != null) {
            database.closeConnection();
            System.out.println("==> Connection to database closed. <==");
        }

    }
}
