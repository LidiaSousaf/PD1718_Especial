package ManagementServer.clientcommunication;

import CommunicationCommons.*;
import CommunicationCommons.remoteexceptions.*;
import DatabaseCommunication.DatabaseCommunication;
import DatabaseCommunication.exceptions.*;
import DatabaseCommunication.models.DbPair;
import DatabaseCommunication.models.DbPlayer;

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

    //------------------------ PLAYERS OPERATIONS -------------------------
    @Override
    public boolean registerPlayer(PlayerRegister playerRegister) throws AlreadyExistsRemoteException, InvalidCredentialsException, RemoteException {
        if (playerRegister.isValid()) {
            DbPlayer newPlayer = new DbPlayer(playerRegister.getUserName(), playerRegister.getName(), playerRegister.getPassword());
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
                DbPlayer player = databaseCommunication.getPlayerByUserName(login.getUserName());
                player.setPassword(login.getPassword());
                player.setIpAddress(login.getIpAddress());
                if (databaseCommunication.login(player)) {

                    System.out.println("> DbPlayer " + login.getUserName() + " logged in.");
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
                DbPlayer player = databaseCommunication.getPlayerByUserName(userName);

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
            DbPlayer player = databaseCommunication.getPlayerByUserName(userName);
            if (databaseCommunication.logout(player)) {
                removeClientReference(userName);
                System.out.println("> DbPlayer " + userName + " logged out.");
                deleteAllPairsForPlayer(player);
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
//        List<DbPlayer> dbPlayers = databaseCommunication.getLoggedPlayers();
//
//        List<LoggedPlayerInfo> playerList = new ArrayList<>(dbPlayers.size());
//
//        for (int i = 0; i < dbPlayers.size(); i++) {
//            DbPlayer dbPlayer = dbPlayers.get(i);
////            System.out.println(dbPlayer.getUserName());
//            boolean hasPair = databaseCommunication.checkIfPlayerIsPaired(dbPlayer);
//            playerList.add(new LoggedPlayerInfo(dbPlayer.getUserName(), dbPlayer.getName(), hasPair));
//        }
//
//        return playerList;

        return internalGetLoggedPlayers();
    }

    //-------------------------- PAIRS OPERATIONS -------------------------
    @Override
    public boolean requestPair(PairRequest pairRequest) throws NotLoggedException, InvalidCredentialsException, AlreadyPairedException, RemoteException {
        try {
            DbPlayer player1 = databaseCommunication.getPlayerByUserName(pairRequest.getPlayer1());
            DbPlayer player2 = databaseCommunication.getPlayerByUserName(pairRequest.getPlayer2());

            if (!player1.isLogged() || !player2.isLogged()) {
                throw new NotLoggedException();
            }

            if (databaseCommunication.checkIfPlayerIsPaired(player1)) {
                throw new AlreadyPairedException("O jogador " + player1.getUserName() + " já tem par formado.");
            }
            if (databaseCommunication.checkIfPlayerIsPaired(player2)) {
                throw new AlreadyPairedException("O jogador " + player1.getUserName() + " já tem par formado.");
            }

            try {
                DbPair pair = databaseCommunication.getPairForPlayers(player1, player2);
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

    @Override
    public synchronized boolean acceptPair(PairRequest pairRequest) throws InvalidCredentialsException, NotLoggedException, AlreadyPairedException, PairNotFoundRemoteException, RemoteException {
        try {
            DbPlayer player1 = databaseCommunication.getPlayerByUserName(pairRequest.getPlayer1());
            DbPlayer player2 = databaseCommunication.getPlayerByUserName(pairRequest.getPlayer2());

            if (!player1.isLogged() || !player2.isLogged()) {
                throw new NotLoggedException();
            }

            if (databaseCommunication.checkIfPlayerIsPaired(player1)) {
                throw new AlreadyPairedException("O jogador " + player1.getUserName() + " já tem par formado com outro jogador.");
            }

            if (databaseCommunication.checkIfPlayerIsPaired(player2)) {
                throw new AlreadyPairedException("O jogador " + player2.getUserName() + " já tem par formado com outro jogador.");
            }

            try {
                DbPair pair = databaseCommunication.getPairForPlayers(player1, player2);

                pairRequest.setFormed(true);

                if (notifyAcceptedPair(pairRequest)) {
                    if (databaseCommunication.completePairFormation(pair)) {
                        cancelAllPendingPairsForPlayer(player1);
                        rejectAllPendingPairsForPlayer(player1);

                        cancelAllPendingPairsForPlayer(player2);
                        rejectAllPendingPairsForPlayer(player2);

                        System.out.println(">---- DbPair formation between " + player1.getUserName()
                                + " and " + player2.getUserName() + " completed ----<");

                        updateLoggedClients();

                        return true;
                    }
                }
            } catch (PairNotFoundException e) {
                throw new PairNotFoundRemoteException();
            }

        } catch (PlayerNotFoundException e) {
            throw new InvalidCredentialsException();
        }
        return false;
    }

    @Override
    public boolean rejectPair(PairRequest pairRequest) throws InvalidCredentialsException, NotLoggedException, PairNotFoundRemoteException, RemoteException {
        try {
            DbPlayer player1 = databaseCommunication.getPlayerByUserName(pairRequest.getPlayer1());
            DbPlayer player2 = databaseCommunication.getPlayerByUserName(pairRequest.getPlayer2());

            if (!player1.isLogged() || !player2.isLogged()) {
                throw new NotLoggedException();
            }

            try {
                DbPair pair = databaseCommunication.getPairForPlayers(player1, player2);

                databaseCommunication.deletePair(pair);
                notifyRejectedPair(pairRequest.getPlayer2(), pairRequest.getPlayer1());

                System.out.println(">---- DbPair request between " + player1.getUserName()
                        + " and " + player2.getUserName() + " was rejected ----<");

                return true;

            } catch (PairNotFoundException e) {
                throw new PairNotFoundRemoteException();
            }

        } catch (PlayerNotFoundException e) {
            throw new InvalidCredentialsException();
        }
    }

    @Override
    public boolean cancelPair(String userName, PairRequest pairRequest) throws InvalidCredentialsException, NotLoggedException, PairNotFoundRemoteException, RemoteException {
        try {
            DbPlayer player1 = databaseCommunication.getPlayerByUserName(pairRequest.getPlayer1());
            DbPlayer player2 = databaseCommunication.getPlayerByUserName(pairRequest.getPlayer2());

            String user1 = player1.getUserName();
            String user2 = player2.getUserName();

            if (!userName.equals(user1) && userName.equals(user2)) {
                throw new InvalidCredentialsException();
            }

            if (!player1.isLogged() || !player2.isLogged()) {
                throw new NotLoggedException();
            }

            try {
                DbPair pair = databaseCommunication.getPairForPlayers(player1, player2);

                String playerToNotify;
                if (!userName.equals(user1)) {
                    playerToNotify = user1;
                } else {
                    playerToNotify = user2;
                }
                databaseCommunication.deletePair(pair);
                notifyCanceledPair(userName, playerToNotify);

                System.out.println(">---- DbPair between " + player1.getUserName()
                        + " and " + player2.getUserName() + " was canceled ----<");

                if (pair.isFormed()) {
                    updateLoggedClients();
                }

                return true;

            } catch (PairNotFoundException e) {
                throw new PairNotFoundRemoteException();
            }

        } catch (PlayerNotFoundException e) {
            throw new InvalidCredentialsException();
        }
    }

    //-------------------------- OTHER METHODS ----------------------------
    private void updateLoggedClients() {

        List<LoggedPlayerInfo> playerList = new ArrayList<>();

        playerList = internalGetLoggedPlayers();

        for (int i = 0; i < clients.size(); i++) {
            try {
                clients.get(i).getClientCallback().updateLoggedPlayers(playerList);
            } catch (RemoteException e) {
                System.err.println("Error updating client " + clients.get(i).getUserName());
            }
        }
    }

    private List<LoggedPlayerInfo> internalGetLoggedPlayers() {
        List<DbPlayer> dbPlayers = databaseCommunication.getLoggedPlayers();

        List<LoggedPlayerInfo> playerList = new ArrayList<>(dbPlayers.size());

        for (int i = 0; i < dbPlayers.size(); i++) {
            DbPlayer dbPlayer = dbPlayers.get(i);
//            System.out.println(dbPlayer.getUserName());
            boolean paired = databaseCommunication.checkIfPlayerIsPaired(dbPlayer);
            playerList.add(new LoggedPlayerInfo(dbPlayer.getUserName(), dbPlayer.getName(), paired));
        }

        return playerList;
    }

    private void notifyNewPairRequest(PairRequest pairRequest) {
        LoggedClientReference clientReference = getClientReference(pairRequest.getPlayer2());
        if (clientReference != null) {
            try {
                clientReference.getClientCallback().notifyNewPairRequest(pairRequest);
            } catch (RemoteException e) {
                System.err.println("Error notifying new pair request to client " + clientReference.getUserName());
            }
        }
    }

    private void notifyRejectedPair(String rejectingPlayer, String playerToNotify) {
        LoggedClientReference reference = getClientReference(playerToNotify);
        if (reference != null) {
            try {
                reference.getClientCallback().notifyRejectedPair(rejectingPlayer);
            } catch (RemoteException e) {
                System.err.println("Error notifying rejection of pair request to client " + playerToNotify);
            }
        }
    }

    private void notifyCanceledPair(String cancelingPlayer, String playerToNotify) {
        LoggedClientReference reference = getClientReference(playerToNotify);
        if (reference != null) {
            try {
                reference.getClientCallback().notifyCanceledPair(cancelingPlayer);
            } catch (RemoteException e) {
                System.err.println("Error notifying canceling of pair request to client " + playerToNotify);
            }
        }
    }

    private boolean notifyAcceptedPair(PairRequest pairRequest) {
        LoggedClientReference reference = getClientReference(pairRequest.getPlayer1());
        if (reference != null) {
            try {
                reference.getClientCallback().notifyAcceptedPair(pairRequest);
                return true;
            } catch (RemoteException e) {
                System.err.println("Error notifying acceptance of pair request to client " + pairRequest.getPlayer1());
            }
        }

        return false;
    }

    private void deleteAllPairsForPlayer(DbPlayer player) {
        List<DbPair> pairList = databaseCommunication.getAllPairsForPlayer(player);

        String opponentName;

        for (DbPair pair : pairList) {
            try {
                boolean sendRejected = false;
                if (player.getId() == pair.getPlayer1Id()) {
                    opponentName = databaseCommunication.getPlayerById(pair.getPlayer2Id()).getUserName();
                } else { //player.getId() == pair.getPlayer2Id()
                    opponentName = databaseCommunication.getPlayerById(pair.getPlayer1Id()).getUserName();
                    if (!pair.isFormed()) {
                        sendRejected = true;
                    }
                }

                if (sendRejected) {
                    notifyRejectedPair(player.getUserName(), opponentName);
                } else {
                    notifyCanceledPair(player.getUserName(), opponentName);
                }
            } catch (PlayerNotFoundException e) {
                System.out.println("Delete pairs for player " + player.getUserName() + ": failed to find opponent.");
            }
        }

        databaseCommunication.deleteAllPairsForPlayer(player);
        System.out.println("--> All pairs for player " + player + " where deleted from DB.");
    }

    private void cancelAllPendingPairsForPlayer(DbPlayer player) {
        List<DbPair> pairList = databaseCommunication.getPendingPairsForPlayer1(player);

        String opponentName;

        for (DbPair pair : pairList) {
            try {
                opponentName = databaseCommunication.getPlayerById(pair.getPlayer2Id()).getUserName();
                notifyCanceledPair(player.getUserName(), opponentName);
            } catch (PlayerNotFoundException e) {
                System.out.println("Cancel pending pairs for player " + player.getUserName() + ": failed to find opponent.");
            }
        }

        databaseCommunication.deletePendingPairsForPlayer1(player);
    }

    private void rejectAllPendingPairsForPlayer(DbPlayer player) {
        List<DbPair> pairList = databaseCommunication.getPendingPairsForPlayer2(player);

        String opponentName;

        for (DbPair pair : pairList) {
            try {
                opponentName = databaseCommunication.getPlayerById(pair.getPlayer1Id()).getUserName();
                notifyRejectedPair(player.getUserName(), opponentName);
            } catch (PlayerNotFoundException e) {
                System.out.println("Reject pending pairs for player " + player.getUserName() + ": failed to find opponent.");
            }
        }

        databaseCommunication.deletePendingPairsForPlayer2(player);
    }

    private LoggedClientReference getClientReference(String userName) {
        for (LoggedClientReference clientReference : clients) {
            if (clientReference.getUserName().equals(userName)) {
                return clientReference;
            }
        }

        return null;
    }

    private void removeClientReference(String userName) {
        LoggedClientReference clientReference = getClientReference(userName);
        if (clientReference != null) {
            clients.remove(clientReference);
        }
    }

    public void logoutAllPlayers() {
        databaseCommunication.logoutAllPlayers();
        System.out.println("--> Logged out all players <--");
    }

    public void deleteAllPairs() {
        databaseCommunication.deleteAllPairs();
        System.out.println("--> Deleted all pairs <--");
    }
}
