/**
 * Created by Lídia on 26/08/2018
 */

package DatabaseCommunication.models;

public class DbPair {

    //------------------------ VARIABLES ----------------------
    private int player1Id;
    private int player2Id;
    private boolean formed;

    //---------------------- CONSTRUCTORS ---------------------
    public DbPair(int player1Id, int player2Id, boolean formed) {
        this.player1Id = player1Id;
        this.player2Id = player2Id;
        this.formed = formed;
    }

    //------------------- GETTERS / SETTERS -------------------

    public int getPlayer1Id() {
        return player1Id;
    }

    public void setPlayer1Id(int player1Id) {
        this.player1Id = player1Id;
    }

    public int getPlayer2Id() {
        return player2Id;
    }

    public void setPlayer2Id(int player2Id) {
        this.player2Id = player2Id;
    }

    public boolean isFormed() {
        return formed;
    }

    public void setFormed(boolean formed) {
        this.formed = formed;
    }
}
