/**
 * Created by Lídia on 30/08/2018
 */

package GameServer.clientcommunication;

import CommunicationCommons.remoteexceptions.GameNotFoundRemoteException;
import DatabaseCommunication.DatabaseCommunication;
import DatabaseCommunication.exceptions.GameNotFoundException;
import DatabaseCommunication.models.DbGame;
import GameServer.GameServer;
import GameServer.game.files.FileUtility;
import GameServer.game.logic.GameModel;
import GameServer.game.logic.ObservableGame;
import GameServer.game.logic.states.AwaitBeginning;

import java.io.File;
import java.io.IOException;
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

            updatePlayers(); //send the initial state of the game to the players

            String str = "--> _STATE_ game between players \"_PLAYER1_\" and \"_PLAYER2_\" <--";
            String state = game.getState() instanceof AwaitBeginning ? "Beginning" : "Resuming";
            str = str.replace("_STATE_", state)
                    .replace("_PLAYER1_", players[0].getDbPlayer().getUserName())
                    .replace("_PLAYER2_", players[1].getDbPlayer().getUserName());
            System.out.println(str);

            game.startGame();
        } catch (GameNotFoundException e) {
            //notify the players that something went wrong
            updatePlayers(new GameNotFoundRemoteException());
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

    @Override
    public void run() {
        startGame();

        while (!stopThread && !GameServer.stopThreads) {
            synchronized (LOCK) {
                //TODO: retrieve commands from each player in its respective turn
                //TODO: update ObservableGame accordingly
            }
        }

        //remove the client references from the TcpServer
        // and close the respective sockets
        removePlayers();
        closeSockets();
    }

    @Override
    public void update(Observable o, Object arg) {
        saveGameProgress();
        updatePlayers();
        //TODO: decide what to send to game clients
        //TODO: send updates to players
        //TODO: check for game end, update the database and delete the progress file
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

    private void updatePlayers() {

    }

    private void updatePlayers(Object data) {
        try {
            for (int i = 0; i < 2; ++i) {
                players[i].getOos().writeObject(data);
                players[i].getOos().flush();
            }
        } catch (IOException e) {
            System.err.println("Error sending data to player: " + e);
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
