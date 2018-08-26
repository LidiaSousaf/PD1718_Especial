package CommunicationCommons;

import CommunicationCommons.remoteexceptions.AlreadyExistsRemoteException;
import CommunicationCommons.remoteexceptions.AlreadyLoggedRemoteException;
import CommunicationCommons.remoteexceptions.InvalidCredentialsException;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientManagementInterface extends Remote {
    boolean registerPlayer(PlayerRegister playerRegister) throws AlreadyExistsRemoteException, InvalidCredentialsException, RemoteException;
    boolean login(PlayerLogin login, RemoteClientInterface client) throws InvalidCredentialsException, AlreadyLoggedRemoteException, RemoteException;
    boolean logout(String userName) throws InvalidCredentialsException, RemoteException;
}
