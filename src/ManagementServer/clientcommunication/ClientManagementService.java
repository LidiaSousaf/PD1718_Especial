package ManagementServer.clientcommunication;

import CommunicationCommons.*;
import CommunicationCommons.remoteexceptions.*;
import DatabaseCommunication.DatabaseCommunication;
import DatabaseCommunication.exceptions.*;
import DatabaseCommunication.models.Pair;
import DatabaseCommunication.models.Player;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class ClientManagementService extends UnicastRemoteObject implements ClientManagementInterface {
    //----------------------------- VARIABLES -----------------------------
    private DatabaseCommunication databaseCommunication;
    private ArrayList<LoggedClientReference> clients;

    //---------------------------- CONSTRUCTOR ----------------------------
    public ClientManagementService(DatabaseCommunication databaseCommunication) throws RemoteException {
        this.databaseCommunication = databaseCommunication;
        this.databaseCommunication.logoutAllPlayers();
        this.databaseCommunication.deleteAllPairs();
        this.clients = new ArrayList<>();
    }

    //--------------------- REMOTE INTERFACE METHODS ----------------------
    @Override
    public boolean registerPlayer(PlayerRegister playerRegister) throws AlreadyExistsRemoteException, InvalidCredentialsException, RemoteException {
        if (playerRegister.isValid()) {
            Player newPlayer = new Player(playerRegister.getUserName(), playerRegister.getName(), playerRegister.getPassword());
            try {
                if (databaseCommunication.registerPlayer(newPlayer)) {
                    System.out.println("> New player " + playerRegister.getUserName() + " registered.");
                    return true;
                }

                return false;

            } catch (InvalidPlayerException e) {
                throw new InvalidCredentialsException("Credenciais de registo inválidas!");
            } catch (PlayerAlreadyExistsException e) {
                throw new AlreadyExistsRemoteException();
            }
        } else {
            throw new InvalidCredentialsException("Credenciais de registo inválidas!");
        }
    }

    @Override
    public synchronized boolean login(PlayerLogin login) throws InvalidCredentialsException, AlreadyLoggedRemoteException, RemoteException {
        if (login.isValid()) {
            try {
                Player player = databaseCommunication.getPlayerByUserName(login.getUserName());
                player.setPassword(login.getPassword());
                player.setIpAddress(login.getIpAddress());
                if (databaseCommunication.login(player)) {

                    System.out.println("> Player " + login.getUserName() + " logged in.");
                    updateLoggedClients();

                    return true;
                }

            } catch (PlayerNotFoundException e) {
                throw new InvalidCredentialsException("Credenciais de login inválidas!");
            } catch (AlreadyLoggedInException e) {
                throw new AlreadyLoggedRemoteException();
            }
        } else {
            throw new InvalidCredentialsException("Credenciais de login inválidas!");
        }
        return false;
    }

    @Override
    public synchronized boolean registerRemoteClient(String userName, RemoteClientInterface clientCallback) throws InvalidCredentialsException, NotLoggedException, RemoteException {
        if (clientCallback != null) {
            try {
                Player player = databaseCommunication.getPlayerByUserName(userName);

                if (!player.isLogged()) {
                    throw new NotLoggedException();
                }

                boolean found = false;
                for (LoggedClientReference clientReference : clients) {
                    if (clientReference.getUserName().equals(userName)) {
                        found = true;
                        clientReference.setClientCallback(clientCallback);
                        break;
                    }
                }

                if (!found) {
                    clients.add(new LoggedClientReference(clientCallback, userName));
                }

                clientCallback.updateLoggedPlayers(getLoggedPlayers());

                return true;

            } catch (PlayerNotFoundException e) {
                throw new InvalidCredentialsException();
            }
        }

        return false;
    }

    @Override
    public synchronized boolean logout(String userName) throws InvalidCredentialsException, RemoteException {
        try {
            Player player = databaseCommunication.getPlayerByUserName(userName);
            if (databaseCommunication.logout(player)) {
                removeClientReference(userName);
                System.out.println("> Player " + userName + " logged out.");
                updateLoggedClients();
                return true;
            }
        } catch (PlayerNotFoundException e) {
            throw new InvalidCredentialsException("O utilizador não foi encontrado!");
        }

        return false;
    }

    @Override
    public List<LoggedPlayerInfo> getLoggedPlayers() throws RemoteException {
        List<Player> dbPlayers = databaseCommunication.getLoggedPlayers();

        List<LoggedPlayerInfo> playerList = new ArrayList<>(dbPlayers.size());

        for (int i = 0; i < dbPlayers.size(); i++) {
            Player dbPlayer = dbPlayers.get(i);
//            System.out.println(dbPlayer.getUserName());
            boolean hasPair = databaseCommunication.checkIfPlayerHasPair(dbPlayer);
            playerList.add(new LoggedPlayerInfo(dbPlayer.getUserName(), dbPlayer.getName(), hasPair));
        }

        return playerList;
    }

    @Override
    public boolean requestPair(PairRequest pairRequest) throws NotLoggedException, InvalidCredentialsException, AlreadyPairedException, RemoteException {
        try {
            Player player1 = databaseCommunication.getPlayerByUserName(pairRequest.getPlayer1());
            Player player2 = databaseCommunication.getPlayerByUserName(pairRequest.getPlayer2());

            if (!player1.isLogged() || !player2.isLogged()) {
                throw new NotLoggedException();
            }

            if (databaseCommunication.checkIfPlayerHasPair(player1)) {
                throw new AlreadyPairedException("O jogador " + player1.getUserName() + " já tem par formado.");
            }
            if (databaseCommunication.checkIfPlayerHasPair(player2)) {
                throw new AlreadyPairedException("O jogador " + player1.getUserName() + " já tem par formado.");
            }

            try {
                Pair pair = databaseCommunication.getPairForPlayers(player1, player2);
                System.out.println(">---- Players " + player1.getUserName()
                        + " and " + player2.getUserName() + " are already paired ----<");
            } catch (PairNotFoundException e) {
                if (databaseCommunication.registerPair(player1, player2)) {
                    System.out.println("> New pair registered for "
                            + player1.getUserName() + " and " + player2.getUserName() + ".");

                    notifyNewPairRequest(pairRequest);

                    return true;
                }
            }

        } catch (PlayerNotFoundException e) {
            throw new InvalidCredentialsException();
        }
        return false;
    }

    //-------------------------- OTHER METHODS ----------------------------
    private void updateLoggedClients() {

        List<LoggedPlayerInfo> playerList = new ArrayList<>();
        try {
            playerList = getLoggedPlayers();
        } catch (RemoteException e) {
            System.err.println("Error obtaining logged clients list");
        }

        for (int i = 0; i < clients.size(); i++) {
            try {
                clients.get(i).getClientCallback().updateLoggedPlayers(playerList);
            } catch (RemoteException e) {
                System.err.println("Error updating client " + clients.get(i).getUserName());
            }
        }
    }

    //TODO: implement the rest of the pair requests operations

    private void notifyNewPairRequest(PairRequest pairRequest){
        for(LoggedClientReference clientReference : clients){
            if(clientReference.getUserName().equals(pairRequest.getPlayer2())){
                try {
                    clientReference.getClientCallback().notifyNewPairRequest(pairRequest);
                } catch (RemoteException e) {
                    System.err.println("Error notifying new pair request to client " + clientReference.getUserName());
                }

                break;
            }
        }
    }

    private void removeClientReference(String userName) {
        for (int i = 0; i < clients.size(); i++) {
            if (clients.get(i).getUserName().equals(userName)) {
                clients.remove(i);
                break;
            }
        }
    }

    public void logoutAllPlayers() {
        databaseCommunication.logoutAllPlayers();
    }

    public void deleteAllPairs() {
        databaseCommunication.deleteAllPairs();
    }
}
