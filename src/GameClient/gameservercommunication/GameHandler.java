/**
 * Created by Lídia on 31/08/2018
 */

package GameClient.gameservercommunication;

import CommunicationCommons.GameCommConstants;
import CommunicationCommons.GameMove;
import CommunicationCommons.remoteexceptions.GameNotFoundRemoteException;
import CommunicationCommons.remoteexceptions.PairTimedOutException;
import GameClient.GlobalController;
import GameLogic.three_in_row.logic.GameModel;
import GameLogic.three_in_row.logic.ObservableGame;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class GameHandler implements Runnable {

    //----------------------------- CONSTANTS -----------------------------
    private static final Object LOCK = new Object();

    //----------------------------- VARIABLES -----------------------------
    private GlobalController controller;
    private ObservableGame game;
    private String gameServerIp;
    private boolean stopThread;
    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    //---------------------------- CONSTRUCTOR ----------------------------
    public GameHandler(GlobalController controller, String gameServerIp) {
        this.controller = controller;
        this.game = controller.getGame();
        this.gameServerIp = gameServerIp;
        this.stopThread = false;
        this.socket = null;
    }

    //---------------------------- START METHODS --------------------------
    private void initializeSocket() {
        try {
            InetAddress gameServerAddr = InetAddress.getByName(gameServerIp);
            socket = new Socket(gameServerAddr, GameCommConstants.TCP_PORT);
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
            socket.setSoTimeout(GameCommConstants.SOCKET_TIMEOUT);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Erro ao criar socket TCP: " + e);
            e.printStackTrace();
            stopThread = true;
        }
    }

    private void startGame() {
        try {
            //send identification to the game server
            oos.writeObject(controller.getLogin());
            oos.flush();

            //receive confirmation from game server
            Object result = ois.readObject();
            if (result instanceof Integer) {
                Integer code = (Integer) result;
                if (!code.equals(GameCommConstants.CONNECTION_ACCEPTED)) {
                    JOptionPane.showMessageDialog(null,
                            "O servidor de jogo rejeitou a ligação.");
                    stopThread = true;
                }
            } else { //assume the object read was an exception
                JOptionPane.showMessageDialog(null,
                        "O servidor de jogo rejeitou a ligação.");
                stopThread = true;
            }

        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null,
                    "Recebido objeto desconhecido do servidor de jogo: " + e);
            stopThread = true;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "Erro ao iniciar sessão no servidor de jogo: " + e);
            e.printStackTrace();
            stopThread = true;
        }
    }

    //------------------------ GAME UPDATE METHODS ------------------------
    private void receiveData() {
        try {
            Object data = ois.readObject();
            if (data instanceof PairTimedOutException) {
                JOptionPane.showMessageDialog(null,
                        "O seu par não se ligou a tempo ao servidor de jogo.");
                game.setInterrupted(true);
                stopThread = true;
            } else if (data instanceof GameModel) {
                updateGameModel((GameModel) data);
            } else if (data instanceof GameNotFoundRemoteException) {
                JOptionPane.showMessageDialog(null,
                        "O servidor de jogo não conseguiu comunicar com a base de dados.");
                game.setInterrupted(true);
                stopThread = true;
            } else {//assume that the server sent some error
                JOptionPane.showMessageDialog(null,
                        "Ocorreu um erro no servidor de jogo. A partida foi interrompida");
                game.setInterrupted(true);
                stopThread = true;
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Recebido objeto desconhecido do servidor de jogo: " + e);
        } catch (SocketTimeoutException e) {
            //Make sure the thread doesn't get stuck trying to read from the socket
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "Ocorreu um erro ao receber dados do servidor de jogo: " + e.getMessage());
            e.printStackTrace();
            game.setInterrupted(true);
            stopThread = true;
        }
    }

    private void updateGameModel(GameModel gameModel) {
        synchronized (LOCK) {
            game.setGameModel(gameModel);

            if (game.isInterrupted() || game.isOver()) {
                //game ended
                stopThread = true;
            }
        }
    }

    //-------------------------- GAME END METHODS -------------------------
    private void terminateThread() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Erro ao fechar socket para servidor de jogo: " + e);
            }
        }
    }

    //----------------------------- RUN METHOD ----------------------------
    @Override
    public void run() {
        initializeSocket();

        if (!stopThread) {
            startGame();
        }

        while (!stopThread) {
            receiveData();
        }

        terminateThread();
        controller.gameEnded();
    }

    //------------------- OUTGOING COMMUNICATION METHODS ------------------
    public void sendMove(GameMove gameMove) {
        if (socket == null) {
            return;
        }

        synchronized (LOCK) {
            if (!game.isInterrupted() && !game.isOver()) {
                if (game.getCurrentPlayerName().equals(controller.getLogin().getUserName())) {
                    try {
                        oos.writeObject(gameMove);
                        oos.flush();
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(null,
                                "Ocorreu um erro ao escrever no socket do servidor de jogo: " + e);

                        e.printStackTrace();
                        game.setInterrupted(true);
                        stopThread = true;
                    }
                }
            }
        }
    }

    //this method is only to be used in case the user closes the window without interrupting the game
    public void sendLogoutToGameServer() {
        if (socket == null) {
            return;
        }

        try {
            oos.writeObject(new GameMove(GameCommConstants.INTERRUPT, 0, 0));
            oos.flush();
        } catch (IOException e) {
//            JOptionPane.showMessageDialog(null,
//                    "Ocorreu um erro ao enviar fim de sessão ao socket do servidor de jogo: " + e);

            e.printStackTrace();
            game.setInterrupted(true);
            stopThread = true;
        }
    }
}
