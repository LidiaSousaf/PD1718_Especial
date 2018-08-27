/**
 * Created by Lídia on 27/08/2018
 */

package CommunicationCommons;

import java.io.Serializable;

public class PairRequest implements Serializable {
    //------------------------ CONSTANTS ----------------------
    public static final Long serialVersionUID = 12L;

    //------------------------ VARIABLES ----------------------
    private String player1;
    private String player2;
    private boolean formed;

    //---------------------- CONSTRUCTORS ---------------------
    public PairRequest(String player1, String player2, boolean formed) {
        this.player1 = player1;
        this.player2 = player2;
        this.formed = formed;
    }

    public PairRequest(String player1, String player2){
        this(player1, player2, false);
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

    public boolean isFormed() {
        return formed;
    }

    public void setFormed(boolean formed) {
        this.formed = formed;
    }
}
