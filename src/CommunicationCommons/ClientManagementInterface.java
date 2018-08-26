package CommunicationCommons;

import CommunicationCommons.remoteexceptions.AlreadyExistsRemoteException;
import CommunicationCommons.remoteexceptions.AlreadyLoggedRemoteException;
import CommunicationCommons.remoteexceptions.InvalidCredentialsException;
import CommunicationCommons.remoteexceptions.NotLoggedException;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ClientManagementInterface extends Remote {
    boolean registerPlayer(PlayerRegister playerRegister) throws AlreadyExistsRemoteException, InvalidCredentialsException, RemoteException;
    boolean login(PlayerLogin login) throws InvalidCredentialsException, AlreadyLoggedRemoteException, RemoteException;
    boolean registerRemoteClient(String userName, RemoteClientInterface clientCallback) throws InvalidCredentialsException, NotLoggedException, RemoteException;
    boolean logout(String userName) throws InvalidCredentialsException, RemoteException;
    List<LoggedPlayerInfo> getLoggedPlayers() throws RemoteException;
}
