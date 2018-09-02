/**
 * Created by Lídia on 30/08/2018
 */

package DatabaseCommunication.models;

public class DbGame {
    //------------------------ CONSTANTS ----------------------
    public static final int INVALID_ID = -1;

    //------------------------ VARIABLES ----------------------
    private int id;
    private int player1Id;
    private int player2Id;
    private Integer winnerId;
    private boolean ended;

    //---------------------- CONSTRUCTORS ---------------------
    public DbGame(int player1Id, int player2Id, Integer winnerId, boolean ended, int id) {
        this.player1Id = player1Id;
        this.player2Id = player2Id;
        this.winnerId = winnerId;
        this.ended = ended;
        this.id = id;
    }

    public DbGame(int player1Id, int player2Id) {
        this(player1Id, player2Id, null, false, INVALID_ID);
    }


    //------------------- GETTERS / SETTERS -------------------
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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

    public Integer getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(Integer winnerId) {
        this.winnerId = winnerId;
    }

    public boolean isEnded() {
        return ended;
    }

    public void setEnded(boolean ended) {
        this.ended = ended;
    }
}
