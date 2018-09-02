package CommunicationCommons;

import CommunicationCommons.remoteexceptions.*;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ClientManagementInterface extends Remote {
    boolean registerPlayer(PlayerRegister playerRegister)
            throws AlreadyExistsRemoteException, InvalidCredentialsException, RemoteException;

    String login(PlayerLogin login)
            throws InvalidCredentialsException, AlreadyLoggedRemoteException, RemoteException;

    boolean registerRemoteClient(String userName, RemoteClientInterface clientCallback)
            throws InvalidCredentialsException, NotLoggedException, RemoteException;

    boolean logout(String userName) throws InvalidCredentialsException, RemoteException;

    List<LoggedPlayerInfo> getLoggedPlayers() throws RemoteException;

    boolean requestPair(PairRequest pairRequest)
            throws NotLoggedException, InvalidCredentialsException, AlreadyPairedException, RemoteException;

    boolean acceptPair(PairRequest pairRequest)
            throws InvalidCredentialsException, NotLoggedException,
            AlreadyPairedException, PairNotFoundRemoteException, RemoteException;

    boolean rejectPair(PairRequest pairRequest)
            throws InvalidCredentialsException, NotLoggedException,
            PairNotFoundRemoteException, RemoteException;

    boolean cancelPair(String userName, PairRequest pairRequest)
            throws InvalidCredentialsException, NotLoggedException,
            PairNotFoundRemoteException, RemoteException;

    String getGameServerAddress() throws NoGameServerException, RemoteException;

    List<GameInfo> getUnfinishedGames(String userName) throws InvalidCredentialsException, RemoteException;

    List<GameInfo> getFinishedGames(String userName) throws InvalidCredentialsException, RemoteException;
}
