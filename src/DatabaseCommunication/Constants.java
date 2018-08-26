/**
 * Created by Lídia on 22/08/2018
 */

package DatabaseCommunication;

public class Constants {
    //--------------------- CONNECTION -------------------
    public static final int DEFAULT_PORT = 3306;
    public static final String CONNECTION_STRING_END = "?autoReconnect=true&useSSL=false&serverTimezone=UTC";

    //----------------------- TABLES ---------------------
    //---------------------- COMMONS ---------------------
    public static final String ID = "Id";
    public static final String PLAYER1_ID = "Player1_Id";
    public static final String PLAYER2_ID = "Player2_Id";

    //------------------- PLAYERS TABLE ------------------
    public static final String PLAYERS_TABLE = "Players";
    public static final String USERNAME = "Username";
    public static final String PASSWORD = "Password";
    public static final String NAME = "Name";
    public static final String IP_ADDRESS = "IpAddress";
    public static final String LOGGED = "Logged";

    //-------------------- PAIRS TABLE -------------------
    public static final String PAIRS_TABLE = "Pairs";
    public static final String FORMED = "Formed";

    //-------------------- GAMES TABLE -------------------
    public static final String GAMES_TABLE = "Games";
    public static final String WINNER_ID = "Winner_Id";
    public static final String ENDED = "Ended";
}
