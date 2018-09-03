package GameClient;

import CommunicationCommons.*;
import CommunicationCommons.remoteexceptions.*;
import GameClient.gameservercommunication.GameHandler;
import GameLogic.three_in_row.logic.ObservableGame;

import javax.swing.*;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;

public class GlobalController extends Observable {

    //----------------------------- VARIABLES -----------------------------
    private ClientManagementInterface clientManagement;
    private PlayerLogin login;
    private String playerName;
    private PairRequest currentPair;
    private ObservableGame game;
    private GameHandler gameHandler;
    private List<PairRequest> incomingRequests;
    private List<PairRequest> outgoingRequests;

    //---------------------------- CONSTRUCTOR ----------------------------
    public GlobalController(String managementAddress) {
        startManagementServerConnection(managementAddress);
        login = null;
        playerName = null;
        currentPair = null;
        game = new ObservableGame();
        game.setInterrupted(true);
        gameHandler = null;
        incomingRequests = new ArrayList<>();
        outgoingRequests = new ArrayList<>();
    }

    //------------------------- GETTERS / SETTERS -------------------------
    public PlayerLogin getLogin() {
        return login;
    }

    public void setLogin(PlayerLogin login) {
        this.login = login;
        setChanged();
        notifyObservers(login);
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
        setChanged();
        notifyObservers(playerName);
    }

    public PairRequest getCurrentPair() {
        return currentPair;
    }

    public void setCurrentPair(PairRequest currentPair) {
        this.currentPair = currentPair;
        setChanged();
        notifyObservers(currentPair);
    }

    public ObservableGame getGame() {
        return game;
    }

//    public void setGame(ObservableGame game) {
//        this.game = game;
//
//        setChanged();
//        notifyObservers();
//    }

    private void setGameHandler(GameHandler gameHandler) {
        this.gameHandler = gameHandler;

        setChanged();
        notifyObservers();
    }

    public boolean isGameRunning() {
        return gameHandler != null;
    }

    public List<PairRequest> getIncomingRequests() {
        return incomingRequests;
    }

    public void addIncomingRequest(PairRequest request) {
        incomingRequests.add(request);

        setChanged();
        notifyObservers();
    }

    public void removeIncomingRequest(PairRequest request) {
        incomingRequests.remove(request);

        setChanged();
        notifyObservers();
    }

    public void removeIncomingRequest(String player1) {
        Iterator<PairRequest> it = incomingRequests.iterator();

        while (it.hasNext()) {
            PairRequest request = it.next();
            if (request.getPlayer1().equals(player1)) {
                it.remove();
            }
        }

        setChanged();
        notifyObservers();
    }

    public void clearIncomingRequests() {
        incomingRequests.clear();

        setChanged();
        notifyObservers();
    }

    public List<PairRequest> getOutgoingRequests() {
        return outgoingRequests;
    }

    public void addOutgoingRequest(PairRequest request) {
        outgoingRequests.add(request);

        setChanged();
        notifyObservers();
    }

    public void removeOutgoingRequest(PairRequest request) {
        outgoingRequests.remove(request);

        setChanged();
        notifyObservers();
    }

    public void removeOutgoingRequest(String player2){
        Iterator<PairRequest> it = outgoingRequests.iterator();

        while (it.hasNext()){
            PairRequest request = it.next();
            if(request.getPlayer2().equals(player2)){
                it.remove();
            }
        }

        setChanged();
        notifyObservers();
    }

    public void clearOutgoingRequests() {
        outgoingRequests.clear();

        setChanged();
        notifyObservers();
    }

    //---------------------- MANAGEMENT COMMUNICATION ---------------------
    private void startManagementServerConnection(String managementAddress) {
        try {
            String managementServiceURL = "rmi://" + managementAddress + "/ClientManagementService";
            clientManagement = (ClientManagementInterface) Naming.lookup(managementServiceURL);

        } catch (NotBoundException | MalformedURLException | RemoteException e) {
            JOptionPane.showMessageDialog(null, "Erro ao estabelecer comunicação com o servidor de gestão:\n" + e.getMessage());
            e.printStackTrace();
            shutdownClient(-1);
        }
    }

    public void login(String userName, String password) {
        String ip = null;

        try {
            ip = InetAddress.getLocalHost().getHostAddress();
            PlayerLogin loginAttempt = new PlayerLogin(userName, password, ip);

            if (!loginAttempt.isValid()) {
                JOptionPane.showMessageDialog(null, "Credenciais de login inválidas!");
                return;
            }

            String name = null;
            name = clientManagement.login(loginAttempt);
            if (name != null) {
                setLogin(loginAttempt);
                setPlayerName(name);
//                JOptionPane.showMessageDialog(null, "Login efetuado com sucesso.");
            }

        } catch (InvalidCredentialsException e) {
            JOptionPane.showMessageDialog(null, "Credenciais de login inválidas!");
        } catch (AlreadyLoggedRemoteException e) {
            JOptionPane.showMessageDialog(null, "O jogador já está logado!");
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(null, "Erro inesperado: " + e.getMessage());
            e.printStackTrace();
            shutdownClient(-1);
        } catch (UnknownHostException e) {
            JOptionPane.showMessageDialog(null,
                    "Erro ao obter o endereço de rede do cliente: " + e.getMessage());
            shutdownClient(-1);
        }
    }

    public void registerClientCallback(RemoteClientInterface clientCallback) {
        try {
            clientManagement.registerRemoteClient(login.getUserName(), clientCallback);
        } catch (InvalidCredentialsException e) {
            JOptionPane.showMessageDialog(null, "O jogador não se encontra registado!");
        } catch (NotLoggedException e) {
            JOptionPane.showMessageDialog(null, "O jogador não se encontra logado!");
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(null, "Erro inesperado: " + e);
            shutdownClient(-1);
        }
    }

    private void logout() {
        try {
            clientManagement.logout(login.getUserName());
        } catch (InvalidCredentialsException e) {
            System.err.println("Credenciais inválidas");
        } catch (RemoteException e) {
            System.err.println(e.getMessage());
        }
    }

    public void registerNewPlayer(String userName, String name, String password) {
        PlayerRegister register = new PlayerRegister(userName, name, password);
        try {
            if (clientManagement.registerPlayer(register)) {
                JOptionPane.showMessageDialog(null, "Registo realizado com sucesso.");
            }
        } catch (InvalidCredentialsException e) {
            JOptionPane.showMessageDialog(null, "Credenciais de registo inválidas!");
        } catch (AlreadyExistsRemoteException e) {
            JOptionPane.showMessageDialog(null, "O utilizador com Username " + userName + " já se encontra registado!");
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(null, "Erro inesperado: " + e.getMessage());
            e.printStackTrace();
            shutdownClient(-1);
        }
    }

    public List<LoggedPlayerInfo> getLoggedPlayers() {
        List<LoggedPlayerInfo> playerList = new ArrayList<>();
        try {
            playerList = clientManagement.getLoggedPlayers();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return playerList;
    }

    public void requestPair(String invitedPlayer) {
//        if (currentPair != null) {
//            return;
//        }

        for (PairRequest request : incomingRequests) {
            if (request.getPlayer1().equals(invitedPlayer)) {
                return; //there is already an incoming request from this player
            }
        }

        for (PairRequest request : outgoingRequests) {
            if (request.getPlayer2().equals(invitedPlayer)) {
                return; //there is already an outgoing request for this player
            }
        }

        try {
            PairRequest newRequest = new PairRequest(login.getUserName(), invitedPlayer);
            clientManagement.requestPair(newRequest);
//            setCurrentPair(newRequest);
            addOutgoingRequest(newRequest);
//                JOptionPane.showMessageDialog(null, "Pedido de par enviado para o jogador " + invitedPlayer);

        } catch (NotLoggedException e) {
            JOptionPane.showMessageDialog(null, "Um dos jogadores não está logado!");
        } catch (InvalidCredentialsException e) {
            JOptionPane.showMessageDialog(null, "Um dos jogadores não está registado!");
        } catch (AlreadyPairedException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(null, "Erro inesperado: " + e.getMessage());
            e.printStackTrace();
            shutdownClient(-1);
        }
    }

    public void cancelPair(PairRequest request) {
        try {
            clientManagement.cancelPair(login.getUserName(), request);
            removeOutgoingRequest(request);
        } catch (InvalidCredentialsException e) {
            JOptionPane.showMessageDialog(null, "Um dos jogadores não está registado!");
        } catch (NotLoggedException e) {
            JOptionPane.showMessageDialog(null, "Um dos jogadores não está logado!");
        } catch (PairNotFoundRemoteException e) {
            JOptionPane.showMessageDialog(null, "O par indicado já não existe na BD!");
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(null, "Erro inesperado: " + e.getMessage());
            e.printStackTrace();
            shutdownClient(-1);
        }
    }

    public void cancelCurrentPair() {
        try {
            clientManagement.cancelPair(login.getUserName(), currentPair);
            setCurrentPair(null);
        } catch (InvalidCredentialsException e) {
            JOptionPane.showMessageDialog(null, "Um dos jogadores não está registado!");
        } catch (NotLoggedException e) {
            JOptionPane.showMessageDialog(null, "Um dos jogadores não está logado!");
        } catch (PairNotFoundRemoteException e) {
            JOptionPane.showMessageDialog(null, "O par indicado já não existe na BD!");
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(null, "Erro inesperado: " + e.getMessage());
            e.printStackTrace();
            shutdownClient(-1);
        }
    }

    public void acceptPair(PairRequest newPairRequest) {
        try {
            clientManagement.acceptPair(newPairRequest);
            newPairRequest.setFormed(true);
            setCurrentPair(newPairRequest);

            clearIncomingRequests();
            clearOutgoingRequests();
        } catch (InvalidCredentialsException e) {
            JOptionPane.showMessageDialog(null, "Um dos jogadores não está registado!");
        } catch (NotLoggedException e) {
            JOptionPane.showMessageDialog(null, "Um dos jogadores não está logado!");
        } catch (AlreadyPairedException e) {
            JOptionPane.showMessageDialog(null, "O jogador "
                    + newPairRequest.getPlayer1() + " já tem par formado com outro jogador.");
        } catch (PairNotFoundRemoteException e) {
            JOptionPane.showMessageDialog(null, "O par indicado já não existe na BD!");
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(null, "Erro inesperado: " + e.getMessage());
            e.printStackTrace();
            shutdownClient(-1);
        }
    }

    public void rejectPair(PairRequest newPairRequest) {
        try {
            clientManagement.rejectPair(newPairRequest);
            removeIncomingRequest(newPairRequest);
//            setCurrentPair(null);
        } catch (InvalidCredentialsException e) {
            JOptionPane.showMessageDialog(null, "Um dos jogadores não está registado!");
        } catch (NotLoggedException e) {
            JOptionPane.showMessageDialog(null, "Um dos jogadores não está logado!");
        } catch (PairNotFoundRemoteException e) {
            JOptionPane.showMessageDialog(null, "O par indicado já não existe na BD!");
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(null, "Erro inesperado: " + e.getMessage());
            e.printStackTrace();
            shutdownClient(-1);
        }
    }

    private String getGameServerIp() {
        try {
            return clientManagement.getGameServerAddress();
        } catch (NoGameServerException e) {
            JOptionPane.showMessageDialog(null, "Não existe nenhum servidor de jogo ligado.");
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(null, "Erro inesperado: " + e.getMessage());
            e.printStackTrace();
            shutdownClient(-1);
        }

        return null;
    }

    public List<GameInfo> getUnfinishedGames() {
        List<GameInfo> gameList = null;
        try {
            gameList = clientManagement.getUnfinishedGames(login.getUserName());
        } catch (InvalidCredentialsException e) {
            JOptionPane.showMessageDialog(null, "O jogador "
                    + login.getUserName() + " não existe na base de dados!");
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(null, "Erro inesperado: " + e.getMessage());
            e.printStackTrace();
            shutdownClient(-1);
        }

        return gameList;
    }

    public List<GameInfo> getFinishedGames() {
        List<GameInfo> gameList = null;
        try {
            gameList = clientManagement.getFinishedGames(login.getUserName());
        } catch (InvalidCredentialsException e) {
            JOptionPane.showMessageDialog(null, "O jogador "
                    + login.getUserName() + " não existe na base de dados!");
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(null, "Erro inesperado: " + e.getMessage());
            e.printStackTrace();
            shutdownClient(-1);
        }

        return gameList;
    }

    public void sendMessage(String target, String message) {
        if (login.getUserName().equals(target)) {
            return;
        }

        try {
            clientManagement.sendMessage(login.getUserName(), target, message);
        } catch (InvalidCredentialsException e) {
            JOptionPane.showMessageDialog(null, "O jogador " + target + " não existe!");
        } catch (NotLoggedException e) {
            JOptionPane.showMessageDialog(null, "O jogador " + target + " não está logado!");
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(null, "Erro inesperado: " + e.getMessage());
            e.printStackTrace();
            shutdownClient(-1);
        }
    }

    //------------------------ GAME COMMUNICATION -------------------------
    public void startGame() {
        if (currentPair != null && currentPair.isFormed()) {
            String gameServerIp = getGameServerIp();
            if (gameServerIp != null && !isGameRunning()) {
                setGameHandler(new GameHandler(this, gameServerIp));
                Thread gameThread = new Thread(gameHandler);
                gameThread.start();
            }
        }
    }

    public void sendMove(GameMove move) {
        if (gameHandler != null && !game.isInterrupted() && !game.isOver()) {
            gameHandler.sendMove(move);
        }
    }

    //-------------------------- OTHER METHODS ----------------------------
    public void shutdownClient(int exitStatus) {
        if (login != null) {
            logout();
        }

        if (gameHandler != null) {
            gameHandler.sendLogoutToGameServer();
        }

        System.exit(exitStatus);

    }

    public void gameEnded() {
        setGameHandler(null);
    }
}
