package ManagementServer.clientcommunication;

import CommunicationCommons.*;
import CommunicationCommons.remoteexceptions.AlreadyExistsRemoteException;
import CommunicationCommons.remoteexceptions.AlreadyLoggedRemoteException;
import CommunicationCommons.remoteexceptions.InvalidCredentialsException;
import CommunicationCommons.remoteexceptions.NotLoggedException;
import DatabaseCommunication.DatabaseCommunication;
import DatabaseCommunication.exceptions.AlreadyLoggedInException;
import DatabaseCommunication.exceptions.InvalidPlayerException;
import DatabaseCommunication.exceptions.PlayerAlreadyExistsException;
import DatabaseCommunication.exceptions.PlayerNotFoundException;
import DatabaseCommunication.models.Player;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ClientManagementService extends UnicastRemoteObject implements ClientManagementInterface {
    //----------------------------- VARIABLES -----------------------------
    private DatabaseCommunication databaseCommunication;
    private ArrayList<LoggedClientReference> clients;

    //---------------------------- CONSTRUCTOR ----------------------------
    public ClientManagementService(DatabaseCommunication databaseCommunication) throws RemoteException {
        this.databaseCommunication = databaseCommunication;
        this.databaseCommunication.logoutAllPlayers();
        this.clients = new ArrayList<>();
    }

    //--------------------- REMOTE INTERFACE METHODS ----------------------
    @Override
    public boolean registerPlayer(PlayerRegister playerRegister) throws AlreadyExistsRemoteException, InvalidCredentialsException, RemoteException {
        if (playerRegister.isValid()) {
            Player newPlayer = new Player(playerRegister.getUserName(), playerRegister.getName(), playerRegister.getPassword());
            try {
                databaseCommunication.registerPlayer(newPlayer);
            } catch (InvalidPlayerException e) {
                throw new InvalidCredentialsException("Credenciais de registo inválidas!");
            } catch (PlayerAlreadyExistsException e) {
                throw new AlreadyExistsRemoteException();
            }
        } else {
            throw new InvalidCredentialsException("Credenciais de registo inválidas!");
        }

        return true;
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
    public boolean registerRemoteClient(String userName, RemoteClientInterface clientCallback) throws InvalidCredentialsException, NotLoggedException, RemoteException {
        if (clientCallback != null) {
            try {
                Player player = databaseCommunication.getPlayerByUserName(userName);

                if(!player.isLogged()){
                    throw new NotLoggedException();
                }

                LoggedClientReference clientReference = new LoggedClientReference(clientCallback, userName);
                if (!clients.contains(clientReference)) {
                    clients.add(clientReference);

                    return true;
                }
            }catch (PlayerNotFoundException e){
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
                clients.get(i).getClientReference().updateLoggedPlayers(playerList);
            } catch (RemoteException e) {
                System.err.println("Error updating client " + clients.get(i).getUserName());
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
}
