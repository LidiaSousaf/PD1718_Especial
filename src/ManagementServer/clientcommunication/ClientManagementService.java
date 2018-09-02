package ManagementServer.clientcommunication;

import CommunicationCommons.*;
import CommunicationCommons.remoteexceptions.*;
import DatabaseCommunication.DatabaseCommunication;
import DatabaseCommunication.exceptions.*;
import DatabaseCommunication.models.DbGame;
import DatabaseCommunication.models.DbPair;
import DatabaseCommunication.models.DbPlayer;
import ManagementServer.gameservercommunication.HeartbeatService;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class ClientManagementService extends UnicastRemoteObject implements ClientManagementInterface {
    //----------------------------- VARIABLES -----------------------------
    private DatabaseCommunication databaseCommunication;
    private ArrayList<LoggedClientReference> clients;
    private HeartbeatService heartbeatService;

    //---------------------------- CONSTRUCTOR ----------------------------
    public ClientManagementService(DatabaseCommunication databaseCommunication, HeartbeatService heartbeatService)
            throws RemoteException {
        this.databaseCommunication = databaseCommunication;
        this.databaseCommunication.deleteAllGames();
        this.databaseCommunication.logoutAllPlayers();
        this.databaseCommunication.deleteAllPairs();
        this.clients = new ArrayList<>();

        this.heartbeatService = heartbeatService;
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
    public synchronized String login(PlayerLogin login) throws InvalidCredentialsException, AlreadyLoggedRemoteException, RemoteException {
        if (login.isValid()) {
            try {
                DbPlayer player = new DbPlayer(login.getUserName(), null, login.getPassword());
//                DbPlayer player = databaseCommunication.getPlayerByUserName(login.getUserName());
                player.setIpAddress(login.getIpAddress());

                DbPlayer playerInDb = databaseCommunication.login(player);
                if (playerInDb != null) {

                    System.out.println("> Player " + login.getUserName() + " logged in.");
                    updateLoggedClients();

                    return playerInDb.getName();
                }

            } catch (PlayerNotFoundException e) {
                throw new InvalidCredentialsException("Credenciais de login inválidas!");
            } catch (AlreadyLoggedInException e) {
                throw new AlreadyLoggedRemoteException();
            }
        } else {
            throw new InvalidCredentialsException("Credenciais de login inválidas!");
        }
        return null;
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
                System.out.println("> Player " + userName + " logged out.");
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
        return internalGetLoggedPlayers();
    }

    //-------------------------- PAIRS OPERATIONS -------------------------
    @Override
    public synchronized boolean requestPair(PairRequest pairRequest) throws NotLoggedException, InvalidCredentialsException, AlreadyPairedException, RemoteException {
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

                if (databaseCommunication.completePairFormation(pair)) {
                    notifyAcceptedPair(pairRequest);

                    cancelAllPendingPairsForPlayer(player1);
                    rejectAllPendingPairsForPlayer(player1);

                    cancelAllPendingPairsForPlayer(player2);
                    rejectAllPendingPairsForPlayer(player2);

                    System.out.println(">---- Pair formation between " + player1.getUserName()
                            + " and " + player2.getUserName() + " completed ----<");

                    updateLoggedClients();

                    return true;
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

                System.out.println(">---- Pair request between " + player1.getUserName()
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
    public synchronized boolean cancelPair(String userName, PairRequest pairRequest) throws InvalidCredentialsException, NotLoggedException, PairNotFoundRemoteException, RemoteException {
        try {
            DbPlayer player1 = databaseCommunication.getPlayerByUserName(pairRequest.getPlayer1());
            DbPlayer player2 = databaseCommunication.getPlayerByUserName(pairRequest.getPlayer2());

            String user1 = player1.getUserName();
            String user2 = player2.getUserName();

            if (!userName.equals(user1) && !userName.equals(user2)) {
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

                System.out.println(">---- Pair between " + player1.getUserName()
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

    //--------------------- GAME HISTORY OPERATIONS -----------------------
    @Override
    public List<GameInfo> getUnfinishedGames(String userName) throws InvalidCredentialsException, RemoteException {
        try {
            DbPlayer player = databaseCommunication.getPlayerByUserName(userName);
            List<DbGame> gameList = databaseCommunication.getUnfinishedGamesForPlayer(player);

            return createGameInfoList(gameList);

        } catch (PlayerNotFoundException e) {
            throw new InvalidCredentialsException();
        }
    }

    @Override
    public List<GameInfo> getFinishedGames(String userName) throws InvalidCredentialsException, RemoteException {
        try {
            DbPlayer player = databaseCommunication.getPlayerByUserName(userName);
            List<DbGame> gameList = databaseCommunication.getFinishedGamesForPlayer(player);

            return createGameInfoList(gameList);

        } catch (PlayerNotFoundException e) {
            throw new InvalidCredentialsException();
        }
    }

    //------------------------- GET GAME SERVER ---------------------------
    @Override
    public String getGameServerAddress() throws NoGameServerException, RemoteException {
        String gameServerIp = heartbeatService.getGameServer();
        if (gameServerIp == null) {
            throw new NoGameServerException();
        }

        return gameServerIp;
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
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                LoggedClientReference clientReference = getClientReference(pairRequest.getPlayer2());
                if (clientReference != null) {
                    try {
                        clientReference.getClientCallback().notifyNewPairRequest(pairRequest);
                    } catch (RemoteException e) {
                        System.err.println("Error notifying new pair request to client " + clientReference.getUserName());
                    }
                }
            }
        });
        t.start();
    }

    private void notifyRejectedPair(String rejectingPlayer, String playerToNotify) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                LoggedClientReference reference = getClientReference(playerToNotify);
                if (reference != null) {
                    try {
                        reference.getClientCallback().notifyRejectedPair(rejectingPlayer);
                    } catch (RemoteException e) {
                        System.err.println("Error notifying rejection of pair request to client " + playerToNotify);
                    }
                }
            }
        });
        t.start();
    }

    private void notifyCanceledPair(String cancelingPlayer, String playerToNotify) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                LoggedClientReference reference = getClientReference(playerToNotify);
                if (reference != null) {
                    try {
                        reference.getClientCallback().notifyCanceledPair(cancelingPlayer);
                    } catch (RemoteException e) {
                        System.err.println("Error notifying canceling of pair request to client " + playerToNotify);
                    }
                }
            }
        });
        t.start();
    }

    private void notifyAcceptedPair(PairRequest pairRequest) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                LoggedClientReference reference = getClientReference(pairRequest.getPlayer1());
                if (reference != null) {
                    try {
                        reference.getClientCallback().notifyAcceptedPair(pairRequest);
                    } catch (RemoteException e) {
                        System.err.println("Error notifying acceptance of pair request to client " + pairRequest.getPlayer1());
                    }
                }

            }
        });
        t.start();
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
        System.out.println("--> All pairs for player " + player.getUserName() + " where deleted from DB.");
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

    private GameInfo createGameInfo(DbGame game) throws PlayerNotFoundException {
        DbPlayer player1 = databaseCommunication.getPlayerById(game.getPlayer1Id());
        DbPlayer player2 = databaseCommunication.getPlayerById(game.getPlayer2Id());

        GameInfo gameInfo = new GameInfo(player1.getUserName(), player2.getUserName());

        if (game.getWinnerId() != null) {
            DbPlayer winner = databaseCommunication.getPlayerById(game.getWinnerId());
            gameInfo.setWinner(winner.getUserName());
        }

        return gameInfo;
    }

    private List<GameInfo> createGameInfoList(List<DbGame> gameList) {
        List<GameInfo> gameInfoList = new ArrayList<>(gameList.size());

        for (DbGame dbGame : gameList) {
            try {
                gameInfoList.add(createGameInfo(dbGame));
            } catch (PlayerNotFoundException e) {
                //one of the players no longer exists in the database -> just ignore
            }
        }

        return gameInfoList;
    }

    public void logoutAllPlayers() {
        for (LoggedClientReference clientReference : clients) {
            try {
                clientReference.getClientCallback().forceLogout();
            } catch (RemoteException e) {
                System.err.println("Error forcing player " + clientReference.getUserName() + " to logout!");
            }
        }
        databaseCommunication.logoutAllPlayers();
        System.out.println("--> Logged out all players <--");
    }

    public void deleteAllPairs() {
        databaseCommunication.deleteAllPairs();
        System.out.println("--> Deleted all pairs <--");
    }
}
