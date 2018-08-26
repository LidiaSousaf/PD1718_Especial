package ManagementServer.clientcommunication;
import CommunicationCommons.ClientManagementInterface;
import CommunicationCommons.PlayerLogin;
import CommunicationCommons.PlayerRegister;
import CommunicationCommons.RemoteClientInterface;
import CommunicationCommons.remoteexceptions.AlreadyExistsRemoteException;
import CommunicationCommons.remoteexceptions.AlreadyLoggedRemoteException;
import CommunicationCommons.remoteexceptions.InvalidCredentialsException;
import DatabaseCommunication.DatabaseCommunication;
import DatabaseCommunication.exceptions.AlreadyLoggedInException;
import DatabaseCommunication.exceptions.InvalidPlayerException;
import DatabaseCommunication.exceptions.PlayerAlreadyExistsException;
import DatabaseCommunication.exceptions.PlayerNotFoundException;
import DatabaseCommunication.models.Player;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

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
    public synchronized boolean login(PlayerLogin login, RemoteClientInterface clientCallback) throws InvalidCredentialsException, AlreadyLoggedRemoteException, RemoteException {
        if (login.isValid()) {
            try {
                Player player = databaseCommunication.getPlayerByUserName(login.getUserName());
                player.setPassword(login.getPassword());
                player.setIpAddress(login.getIpAddress());
                if(databaseCommunication.login(player)) {
                    if (clientCallback != null) {
                        LoggedClientReference clientReference = new LoggedClientReference(clientCallback, login.getUserName());
                        if (!clients.contains(clientReference)) {
                            clients.add(clientReference);
                        }
                    }
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
    public synchronized boolean logout(String userName) throws InvalidCredentialsException, RemoteException {
        try {
            Player player = databaseCommunication.getPlayerByUserName(userName);
            if(databaseCommunication.logout(player)) {
                removeClientReference(userName);
                updateLoggedClients();
                return true;
            }
        } catch (PlayerNotFoundException e) {
            throw new InvalidCredentialsException("O utilizador não foi encontrado!");
        }

        return false;
    }

    //-------------------------- OTHER METHODS ----------------------------
    private void updateLoggedClients() {
        //TODO: inform logged players that a new player just logged in
        for(int i = 0; i<clients.size(); i++){
            try {
                clients.get(i).getClientReference().updateLoggedPlayers();
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
