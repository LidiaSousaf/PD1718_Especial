/**
 * Created by Lídia on 30/08/2018
 */

package GameServer.clientcommunication;

import CommunicationCommons.GameCommConstants;
import CommunicationCommons.GameMove;
import CommunicationCommons.remoteexceptions.GameNotFoundRemoteException;
import DatabaseCommunication.DatabaseCommunication;
import DatabaseCommunication.exceptions.GameNotFoundException;
import DatabaseCommunication.models.DbGame;
import GameLogic.three_in_row.logic.states.AwaitPlacement;
import GameServer.GameServer;
import GameLogic.three_in_row.files.FileUtility;
import GameLogic.three_in_row.logic.GameModel;
import GameLogic.three_in_row.logic.ObservableGame;
import GameLogic.three_in_row.logic.states.AwaitBeginning;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Observable;
import java.util.Observer;

public class GameInstance implements Runnable, Observer {

    //----------------------------- CONSTANTS -----------------------------
    private final Object LOCK = new Object();

    //----------------------------- VARIABLES -----------------------------
    private Client[] players;
    private DatabaseCommunication database;
    private TcpServer server;
    private ObservableGame game;
    private DbGame dbGame;
    private File gameProgressFile;
    private boolean stopThread;

    //---------------------------- CONSTRUCTOR ----------------------------
    public GameInstance(Client[] players, DatabaseCommunication database, TcpServer server) {
        this.players = players;
        this.database = database;
        this.server = server;
        this.game = new ObservableGame();
        this.stopThread = false;
    }

    //------------------------- GAME START METHODS ------------------------
    private void startGame() {
        try {
            game.setGameModel(obtainGameModel());

            gameProgressFile = getProgressFile();

            game.addObserver(this);

            String str = "--> _STATE_ game between players \"_PLAYER1_\" and \"_PLAYER2_\" <--";
            String state = game.getState() instanceof AwaitBeginning ? "Beginning" : "Resuming";
            str = str.replace("_STATE_", state)
                    .replace("_PLAYER1_", players[0].getDbPlayer().getUserName())
                    .replace("_PLAYER2_", players[1].getDbPlayer().getUserName());
            System.out.println(str);

            game.startGame();
        } catch (GameNotFoundException e) {
            //notify the players that something went wrong
            sendError(new GameNotFoundRemoteException());
            stopThread = true;
        }
    }

    private GameModel obtainGameModel() throws GameNotFoundException {
        GameModel gameModel = null;

        try {
            //see if there's an unfinished game for this two players in the DB
            dbGame = database.getUnfinishedGameForPlayers(players[0].getDbPlayer(), players[1].getDbPlayer());

            //verify the players positions in the array to match player1 and player2 in the DbGame
            verifyPlayersPosition();

            try {
                //attempt to retrieve a saved game
                gameProgressFile = getProgressFile();
                gameModel = (GameModel) FileUtility.retrieveGameFromFile(gameProgressFile);
            } catch (ClassNotFoundException | IOException e) {
                System.out.println("Error recovering saved game: " + e);
                System.out.println("Creating new one...");

                //saved game couldn't be recovered -> create new one
                gameModel = createNewGameModel();
            }

        } catch (GameNotFoundException e) {
            try {
                //No unfinished game was found -> start a new one
                database.createGame(players[0].getDbPlayer(), players[1].getDbPlayer());
                dbGame = database.getUnfinishedGameForPlayers(players[0].getDbPlayer(), players[1].getDbPlayer());

                System.out.println("--> New game started for players \"" + players[0].getDbPlayer().getUserName()
                        + "\" and \"" + players[1].getDbPlayer().getUserName() + "\".");

                gameModel = createNewGameModel();

            } catch (GameNotFoundException e1) {
                System.out.println("Some error must have occurred while registering a new game in the database...");
                throw e1;
            }

        }

        return gameModel;
    }

    private GameModel createNewGameModel() {
        GameModel gameModel = new GameModel();
        gameModel.setPlayerName(1, players[0].getDbPlayer().getUserName());
        gameModel.setPlayerName(2, players[1].getDbPlayer().getUserName());

        return gameModel;
    }

    private void verifyPlayersPosition() {
        if (dbGame.getPlayer1Id() == players[1].getDbPlayer().getId()) {
            //the players positions were not correct -> switch them
            Client temp = players[0];
            players[0] = players[1];
            players[1] = temp;
        }
    }

    //------------------------------ RUN METHOD ---------------------------
    @Override
    public void run() {
        try {
            players[0].getSocket().setSoTimeout(GameCommConstants.SOCKET_TIMEOUT);
            players[1].getSocket().setSoTimeout(GameCommConstants.SOCKET_TIMEOUT);

            startGame();
        } catch (SocketException e) {
            System.err.println("Error setting players sockets timeout: " + e);
            sendError(GameCommConstants.CONNECTION_REFUSED);
            stopThread = true;
        }

        while (!stopThread && !GameServer.stopThreads) {
            synchronized (LOCK) {
                //retrieve moves from each player in its respective turn
                //update ObservableGame accordingly
                int pIndex = game.getNumCurrentPlayer() - 1; //index of the current player, managed by the game logic

                try {
                    //expect to receive a GameMove from the player
                    GameMove move = (GameMove) players[pIndex].getOis().readObject();
                    handleGameMove(move);

                } catch (ClassNotFoundException e) {
                    System.err.println("Error - received unknown object: " + e);
                } catch (SocketTimeoutException e) {
                    //Make sure the thread doesn't get indefinitely stuck in the loop
                } catch (SocketException e) {
                    System.err.println("TCP socket error in game instance: " + e);
                    stopThread = true;
                    sendError(GameCommConstants.INTERRUPT);
                } catch (IOException e) {
                    System.err.println("IOException in game instance: " + e);
                    stopThread = true;
                    sendError(GameCommConstants.INTERRUPT);
                }
            }
        }

        //remove the client references from the TcpServer
        // and close the respective sockets
        removePlayers();
        closeSockets();
    }

    //------------------------ GAME MANAGEMENT METHODS --------------------
    private void handleGameMove(GameMove move) {
        Integer action = move.getAction();
        if (action.equals(GameCommConstants.MAKE_MOVE)) {
            if (game.getState() instanceof AwaitPlacement) {
                game.placeToken(move.getRow(), move.getRow());
            } else {
                game.returnToken(move.getRow(), move.getCol());
            }
        } else if (action.equals(GameCommConstants.INTERRUPT)) {
            game.setInterrupted(true);
        } else if (action.equals(GameCommConstants.GIVE_UP)) {
            game.quit();
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        saveGameProgress();
        updatePlayers();

        if (game.isInterrupted()) { //game was interrupted by order of one of the players
            System.out.println("--> Game between players \"" + players[0].getDbPlayer().getUserName() +
                    "\" and \"" + players[1].getDbPlayer().getUserName() + "\" was interrupted. <--");
            stopThread = true;

        } else if (game.isOver()) { //game was effectively concluded
            int winnerIndex = game.getWinnerIndex();

            System.out.println("--> Game between players \"" + players[0].getDbPlayer().getUserName() +
                    "\" and \"" + players[1].getDbPlayer().getUserName() + "\" ended. <--");
            System.out.println("---> The Winner was "
                    + players[winnerIndex].getDbPlayer().getUserName()
                    + "! <---");

            //set game as ended in the database
            dbGame.setEnded(true);
            dbGame.setWinnerId(players[winnerIndex].getDbPlayer().getId());
            database.finishGame(dbGame);

            //delete the game progress file
            deleteGameProgress();

            stopThread = true;
        }
    }

    //----------------------- SOCKET RELATED METHODS ----------------------
    private void removePlayers() {
        server.removeClientFromList(players[0].getDbPlayer().getId());
        server.removeClientFromList(players[1].getDbPlayer().getId());
    }

    private void closeSockets() {
        for (int i = 0; i < players.length; i++) {
            try {
                if (players[i].getSocket() != null) {
                    players[i].getSocket().close();
                }
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e);
            }
        }
    }

    //send the game model by default
    private void updatePlayers() {
        for (int i = 0; i < 2; i++) {
            try {
                players[i].getOos().writeObject(game.getGameModel());
                players[i].getOos().flush();
            } catch (IOException e) {
                System.err.println("Error sending game update to player: " + e);
            }
        }
    }

    private void sendError(Object data) {
        for (int i = 0; i < 2; i++) {
            try {
                players[i].getOos().writeObject(data);
                players[i].getOos().flush();
            } catch (IOException e) {
                System.err.println("Error sending data to player: " + e);
            }
        }

    }

    //------------------------ FILE RELATED METHODS -----------------------
    private File getProgressFile() {
        String fileName = dbGame.getId() + ".bin";
        return new File(FileUtility.FILE_PATH + File.separator + fileName);
    }

    private void saveGameProgress() {
        try {
            FileUtility.saveGameToFile(gameProgressFile, game.getGameModel());
        } catch (IOException e) {
            System.err.println("Error saving game progress file \""
                    + gameProgressFile.getName() + "\":" + e);
        }
    }

    private void deleteGameProgress() {
        try {
            if (FileUtility.removeFile(gameProgressFile.getCanonicalPath())) {
                System.out.println("Game progress file \""
                        + gameProgressFile.getName() + "\" successfully removed.");
            } else {
                System.err.println("Error eliminating game progress file \""
                        + gameProgressFile.getName() + "\"");
            }

        } catch (IOException e) {
            System.err.println("Error eliminating game progress file \""
                    + gameProgressFile.getName() + "\": " + e);
        }
    }
}
