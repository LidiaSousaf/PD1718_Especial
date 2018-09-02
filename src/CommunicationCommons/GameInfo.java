/**
 * Created by Lídia on 02/09/2018
 */

package CommunicationCommons;

import java.io.Serializable;

public class GameInfo implements Serializable {
    //------------------------ CONSTANTS ----------------------
    public static final Long serialVersionUID = 7L;

    //------------------------ VARIABLES ----------------------
    private String player1;
    private String player2;
    private String winner;

    //---------------------- CONSTRUCTORS ---------------------
    public GameInfo(String player1, String player2, String winner) {
        this.player1 = player1;
        this.player2 = player2;
        this.winner = winner;
    }

    public GameInfo(String player1, String player2) {
        this(player1, player2, null);
    }

    //------------------- GETTERS / SETTERS -------------------
    public String getPlayer1() {
        return player1;
    }

    public void setPlayer1(String player1) {
        this.player1 = player1;
    }

    public String getPlayer2() {
        return player2;
    }

    public void setPlayer2(String player2) {
        this.player2 = player2;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }
}
