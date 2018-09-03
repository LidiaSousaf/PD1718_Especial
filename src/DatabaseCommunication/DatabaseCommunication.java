/**
 * Created by Lídia on 22/08/2018
 */

package DatabaseCommunication;

import DatabaseCommunication.models.DbGame;
import DatabaseCommunication.models.DbPair;
import DatabaseCommunication.models.DbPlayer;
import DatabaseCommunication.exceptions.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseCommunication {

    //------------------------ VARIABLES ----------------------
    private String dbUserName;
    private String dbPassword;
    private String databaseName;
    private String dbIpAddress;
    private int dbPort;
    private Connection connection;

    //------------------------ CONSTRUCTORS -------------------
    public DatabaseCommunication(String databaseName, String dbUserName, String dbPassword, String dbIpAddress, int dbPort) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("The dependency needed to communicate with the database doesn't exist:\n" + e);
        }
        this.dbUserName = dbUserName;
        this.dbPassword = dbPassword;
        this.databaseName = databaseName;
        this.dbIpAddress = dbIpAddress;
        this.dbPort = dbPort;
    }

    public DatabaseCommunication(String databaseName, String dbUserName, String dbPassword, String dbIpAddress) {
        this(databaseName, dbUserName, dbPassword, dbIpAddress, Constants.DEFAULT_PORT);
    }

    //--------------------- GETTERS / SETTERS -----------------
    public String getDbUserName() {
        return dbUserName;
    }

    public void setDbUserName(String dbUserName) {
        this.dbUserName = dbUserName;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getDbIpAddress() {
        return dbIpAddress;
    }

    public void setDbIpAddress(String dbIpAddress) {
        this.dbIpAddress = dbIpAddress;
    }

    public int getDbPort() {
        return dbPort;
    }

    public void setDbPort(int dbPort) {
        this.dbPort = dbPort;
    }

    //-------------------- DATABASE CONNECTION ----------------
    public boolean connect() {
        String connectionString = "jdbc:mysql://" + dbIpAddress + ":" + dbPort + "/"
                + databaseName + Constants.CONNECTION_STRING_END;
//        System.out.println(connectionString);
        try {
            connection = DriverManager.getConnection(connectionString, dbUserName, dbPassword);
        } catch (SQLException e) {
//            e.printStackTrace();
            System.err.println("Connection to database failed: " + e);
            return false;
        }

        return true;
    }

    public void closeConnection() {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        connection = null;
    }

    //---------------------- PLAYERS TABLE --------------------
    public DbPlayer getPlayerByUserName(String userName) throws PlayerNotFoundException {
        String query = "SELECT * FROM " + Constants.PLAYERS_TABLE + " WHERE " + Constants.USERNAME + " = '" + userName + "';";

        DbPlayer player = parsePlayer(query, userName);

        if (!player.getUserName().equals(userName)) {
            throw new PlayerNotFoundException(userName);
        }

        return player;
    }

    public DbPlayer getPlayerById(int id) throws PlayerNotFoundException {
        if (id < 1) {
            throw new PlayerNotFoundException(id);
        }
        String query = "SELECT * FROM " + Constants.PLAYERS_TABLE + " WHERE " + Constants.ID + " = '" + id + "';";

        return parsePlayer(query, "" + id);
    }

    public DbPlayer parsePlayer(String query, String userName) throws PlayerNotFoundException {
        ResultSet result = executeQuery(query);

        if (result != null) {
            try {
                if (result.next()) {
                    String ip = result.getString(Constants.IP_ADDRESS);
                    if (result.wasNull()) {
                        ip = "";
                    }

                    return new DbPlayer(
                            result.getString(Constants.USERNAME),
                            result.getString(Constants.NAME),
                            result.getString(Constants.PASSWORD),
                            ip,
                            result.getInt(Constants.ID),
                            result.getInt(Constants.LOGGED) != 0);
                } else {
                    throw new PlayerNotFoundException(userName);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                throw new PlayerNotFoundException(userName);
            }
        }
        return null;
    }

    public boolean playerExists(String userName) {
        try {
            getPlayerByUserName(userName);
        } catch (PlayerNotFoundException e) {
            return false;
        }

        return true;
    }

    public boolean registerPlayer(DbPlayer player) throws InvalidPlayerException, PlayerAlreadyExistsException {
        if (!player.isValid()) {
            throw new InvalidPlayerException();
        }
        if (playerExists(player.getUserName())) {
            throw new PlayerAlreadyExistsException();
        }

        String ip = player.getIpAddress();
        if (ip == null || ip.isEmpty()) {
            ip = "NULL";
        } else {
            ip = "'" + ip + "'";
        }

        String sql = "INSERT INTO " + Constants.PLAYERS_TABLE + " (" + Constants.USERNAME + ", " + Constants.NAME
                + ", " + Constants.PASSWORD + ", " + Constants.IP_ADDRESS + ") VALUES ('"
                + player.getUserName() + "', '" + player.getName() + "', '"
                + player.getPassword() + "', " + ip + ");";

        return execute(sql);
    }

    public boolean deletePlayer(String userName) throws PlayerNotFoundException {
        getPlayerByUserName(userName);
        String sql = "DELETE FROM " + Constants.PLAYERS_TABLE + " WHERE " + Constants.USERNAME + " = '" + userName + "';";
        return execute(sql);
    }

    public boolean deleteAllPlayers() {
        String sql = "DELETE FROM " + Constants.PLAYERS_TABLE + ";";

        return execute(sql);
    }

    public DbPlayer login(DbPlayer player) throws PlayerNotFoundException, AlreadyLoggedInException {

        DbPlayer playerInDb = getPlayerByUserName(player.getUserName());

        if (playerInDb.isLogged()) {
            throw new AlreadyLoggedInException();
        }

        if (playerInDb.getPassword().equals(player.getPassword())) {

            String sql = "UPDATE " + Constants.PLAYERS_TABLE + " SET " + Constants.LOGGED + " = '1', "
                    + Constants.IP_ADDRESS + " = '" + player.getIpAddress()
                    + "' WHERE " + Constants.USERNAME + " = '" + player.getUserName() + "';";

            boolean result = execute(sql);
            if (result) {
                player.setLogged(true);
            }

            return playerInDb;

        } else {
            throw new PlayerNotFoundException();
        }
    }

    public boolean logout(DbPlayer player) throws PlayerNotFoundException {
        if (player == null) {
            throw new PlayerNotFoundException();
        }

        getPlayerByUserName(player.getUserName());

        String sql = "UPDATE " + Constants.PLAYERS_TABLE + " SET " + Constants.LOGGED + " = '0', "
                + Constants.IP_ADDRESS + " = " + "NULL"
                + " WHERE " + Constants.USERNAME + " = '"
                + player.getUserName() + "';";

        boolean result = execute(sql);
        if (result) {
            player.setLogged(false);
        }

        return result;
    }

    public void logoutAllPlayers() {
        String sql = "UPDATE " + Constants.PLAYERS_TABLE + " SET "
                + Constants.LOGGED + " = '0', "
                + Constants.IP_ADDRESS + " = " + "NULL"
                + " WHERE " + Constants.LOGGED + " != '0';";

        execute(sql);
    }

    public List<DbPlayer> getLoggedPlayers() {
        String query = "SELECT * FROM " + Constants.PLAYERS_TABLE + " WHERE " + Constants.LOGGED + " = '1';";
        ResultSet result = executeQuery(query);
        return parsePlayerList(result);
    }

    private List<DbPlayer> parsePlayerList(ResultSet result) {
        List<DbPlayer> playerList = new ArrayList<>();

        if (result != null) {
            try {
                while (result.next()) {
                    String ip = result.getString(Constants.IP_ADDRESS);
                    if (result.wasNull())
                        ip = "";
                    playerList.add(new DbPlayer(result.getString(Constants.USERNAME), result.getString(Constants.NAME),
                            result.getString(Constants.PASSWORD), ip,
                            result.getInt(Constants.ID), true));
                }
            } catch (SQLException e) {
                System.err.println("Error parsing player list: " + e);
            }
        }

        return playerList;
    }

    //----------------------- PAIRS TABLE ---------------------
    public boolean checkIfPlayerIsPaired(DbPlayer player) {
        String query = "SELECT * FROM " + Constants.PAIRS_TABLE + " WHERE (" +
                Constants.PLAYER1_ID + " = '" + player.getId() + "' OR " +
                Constants.PLAYER2_ID + " = '" + player.getId() + "') AND " +
                Constants.FORMED + " = '1';";
        ResultSet result = executeQuery(query);

        return parsePairList(result).size() > 0;
    }

    public DbPair getCurrentPairForPlayer(DbPlayer player) throws PairNotFoundException {
        String query = "SELECT * FROM " + Constants.PAIRS_TABLE + " WHERE (" +
                Constants.PLAYER1_ID + " = '" + player.getId() + "' OR " +
                Constants.PLAYER2_ID + " = '" + player.getId() + "') AND " +
                Constants.FORMED + " = '1';";

        ResultSet result = executeQuery(query);

        return parsePair(result);
    }

    public DbPair getPairForPlayers(DbPlayer player1, DbPlayer player2) throws PairNotFoundException {
        String query = "SELECT * FROM " + Constants.PAIRS_TABLE + " WHERE (" +
                Constants.PLAYER1_ID + " = '" + player1.getId() + "' AND " +
                Constants.PLAYER2_ID + " = '" + player2.getId() + "') OR (" +
                Constants.PLAYER1_ID + " = '" + player2.getId() + "' AND " +
                Constants.PLAYER2_ID + " = '" + player1.getId() + "');";

        return parsePair(executeQuery(query));
    }

    public DbPair getPair(DbPair pair) throws PairNotFoundException {
        String query = "SELECT * FROM " + Constants.PAIRS_TABLE + " WHERE "
                + Constants.PLAYER1_ID + " = '" + pair.getPlayer1Id() + "' AND "
                + Constants.PLAYER2_ID + " = '" + pair.getPlayer2Id() + "';";
        return parsePair(executeQuery(query));
    }

    public List<DbPair> getAllPairsForPlayer(DbPlayer player) {
        String query = "SELECT * FROM " + Constants.PAIRS_TABLE + " WHERE " +
                Constants.PLAYER1_ID + " = '" + player.getId() + "' OR " +
                Constants.PLAYER2_ID + " = '" + player.getId() + "';";

        ResultSet result = executeQuery(query);

        return parsePairList(result);
    }

    public List<DbPair> getPendingPairsForPlayer1(DbPlayer player1) {
        String query = "SELECT * FROM " + Constants.PAIRS_TABLE + " WHERE " +
                Constants.PLAYER1_ID + " = '" + player1.getId() + "' AND " +
                Constants.FORMED + " = '0';";

        ResultSet result = executeQuery(query);

        return parsePairList(result);
    }

    public List<DbPair> getPendingPairsForPlayer2(DbPlayer player2) {
        String query = "SELECT * FROM " + Constants.PAIRS_TABLE + " WHERE " +
                Constants.PLAYER2_ID + " = '" + player2.getId() + "' AND " +
                Constants.FORMED + " = '0';";

        ResultSet result = executeQuery(query);

        return parsePairList(result);
    }

    private DbPair parsePair(ResultSet result) throws PairNotFoundException {
        if (result != null) {
            try {
                if (result.next()) {

                    return new DbPair(
                            result.getInt(Constants.PLAYER1_ID),
                            result.getInt(Constants.PLAYER2_ID),
                            result.getInt(Constants.FORMED) == 1);
                } else {
                    throw new PairNotFoundException();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                throw new PairNotFoundException();
            }
        } else {
            throw new PairNotFoundException();
        }
    }

    private List<DbPair> parsePairList(ResultSet result) {
        List<DbPair> pairList = new ArrayList<>();

        if (result != null) {
            try {
                while (result.next()) {
                    pairList.add(new DbPair(result.getInt(Constants.PLAYER1_ID),
                            result.getInt(Constants.PLAYER2_ID),
                            result.getInt(Constants.FORMED) == 1));
                }
            } catch (SQLException e) {
                System.err.println("Error parsing pair list: " + e);
            }
        }

        return pairList;
    }

    public boolean deleteAllPairsForPlayer(DbPlayer player) {
        String sql = "DELETE FROM " + Constants.PAIRS_TABLE + " WHERE "
                + Constants.PLAYER1_ID + " = '" + player.getId() + "' OR "
                + Constants.PLAYER2_ID + "='" + player.getId() + "';";
        return execute(sql);
    }

    public boolean deletePendingPairsForPlayer1(DbPlayer player1) {
        String sql = "DELETE FROM " + Constants.PAIRS_TABLE + " WHERE "
                + Constants.PLAYER1_ID + " = '" + player1.getId() + "' AND "
                + Constants.FORMED + "='0';";
        return execute(sql);
    }

    public boolean deletePendingPairsForPlayer2(DbPlayer player2) {
        String sql = "DELETE FROM " + Constants.PAIRS_TABLE + " WHERE "
                + Constants.PLAYER2_ID + " = '" + player2.getId() + "' AND "
                + Constants.FORMED + "='0';";
        return execute(sql);
    }

    public boolean deletePair(DbPair pair) {
        String sql = "DELETE FROM " + Constants.PAIRS_TABLE + " WHERE "
                + Constants.PLAYER1_ID + " = '" + pair.getPlayer1Id() + "' AND "
                + Constants.PLAYER2_ID + "='" + pair.getPlayer2Id() + "';";
        return execute(sql);
    }

    public boolean registerPair(DbPlayer player1, DbPlayer player2) {
        String sql = "INSERT INTO " + Constants.PAIRS_TABLE + " ("
                + Constants.PLAYER1_ID + ", " + Constants.PLAYER2_ID
                + ", " + Constants.FORMED + ") VALUES ("
                + player1.getId() + ", " + player2.getId() + ", 0);";

        return execute(sql);
    }

    public boolean deleteAllPairs() {
        String sql = "DELETE FROM " + Constants.PAIRS_TABLE + ";";

        return execute(sql);
    }

    public boolean completePairFormation(DbPair pair) {
        String sql = "UPDATE " + Constants.PAIRS_TABLE + " SET " + Constants.FORMED
                + " = '1' WHERE " + Constants.PLAYER1_ID + " = '" + pair.getPlayer1Id()
                + "' AND " + Constants.PLAYER2_ID + " = '" + pair.getPlayer2Id() + "';";

        boolean result = execute(sql);
        if (result) {
            pair.setFormed(true);
        }
        return result;
    }

    //----------------------- GAMES TABLE ---------------------
    public boolean createGame(DbPlayer player1, DbPlayer player2) {
        String sql = "INSERT INTO " + Constants.GAMES_TABLE + " (" + Constants.PLAYER1_ID
                + ", " + Constants.PLAYER2_ID + ", " + Constants.ENDED + ") VALUES ("
                + player1.getId() + ", " + player2.getId() + ", 0);";

        return execute(sql);
    }

    public DbGame getUnfinishedGameForPlayers(DbPlayer player1, DbPlayer player2) throws GameNotFoundException {
        String query = "SELECT * FROM " + Constants.GAMES_TABLE + " WHERE (("
                + Constants.PLAYER1_ID + " = '" + player1.getId() + "' AND "
                + Constants.PLAYER2_ID + " = '" + player2.getId() + "') OR ("
                + Constants.PLAYER1_ID + " = '" + player2.getId() + "' AND "
                + Constants.PLAYER2_ID + " = '" + player1.getId() + "')) AND "
                + Constants.ENDED + " = '0';";

        ResultSet result = executeQuery(query);

        return parseGame(result);
    }

    public List<DbGame> getAllGamesForPlayer(DbPlayer player) {
        String query = "SELECT * FROM " + Constants.GAMES_TABLE + " WHERE "
                + Constants.PLAYER1_ID + " = '" + player.getId() + "' OR "
                + Constants.PLAYER2_ID + " = '" + player.getId() + "';";
        ResultSet result = executeQuery(query);

        return parseGameList(result);
    }

    public List<DbGame> getUnfinishedGamesForPlayer(DbPlayer player) {
        String query = "SELECT * FROM " + Constants.GAMES_TABLE + " WHERE "
                + Constants.PLAYER1_ID + " = '" + player.getId() + "' OR "
                + Constants.PLAYER2_ID + " = '" + player.getId() + "' AND "
                + Constants.ENDED + " = '0';";
        ResultSet result = executeQuery(query);

        return parseGameList(result);
    }

    public List<DbGame> getFinishedGamesForPlayer(DbPlayer player) {
        String query = "SELECT * FROM " + Constants.GAMES_TABLE + " WHERE "
                + Constants.PLAYER1_ID + " = '" + player.getId() + "' OR "
                + Constants.PLAYER2_ID + " = '" + player.getId() + "' AND "
                + Constants.ENDED + " = '1';";
        ResultSet result = executeQuery(query);

        return parseGameList(result);
    }

    public boolean finishGame(DbGame game) {
        String sql = "UPDATE " + Constants.GAMES_TABLE + " SET "
                + Constants.WINNER_ID + " = '" + game.getWinnerId() + "', "
                + Constants.ENDED + " = '1' WHERE "
                + Constants.ID + " = '" + game.getId() + "';";

        return execute(sql);
    }

    private DbGame parseGame(ResultSet result) throws GameNotFoundException {
        if (result != null) {
            try {
                if (result.next()) {
//                    int winnerId = result.getInt(Constants.WINNER_ID);
//                    if (result.wasNull()) {
//                        winnerId = DbPlayer.INVALID_ID;
//                    }
                    return new DbGame(result.getInt(Constants.PLAYER1_ID),
                            result.getInt(Constants.PLAYER2_ID),
                            result.getInt(Constants.WINNER_ID),
                            result.getInt(Constants.ENDED) == 1,
                            result.getInt(Constants.ID));
                } else {
                    throw new GameNotFoundException();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                throw new GameNotFoundException();
            }
        } else {
            throw new GameNotFoundException();
        }
    }

    private List<DbGame> parseGameList(ResultSet result) {
        List<DbGame> gameList = new ArrayList<>();

        if (result != null) {
            try {
                while (result.next()) {
//                    int winnerId = result.getInt(Constants.WINNER_ID);
//                    if (result.wasNull()) {
//                        winnerId = DbPlayer.INVALID_ID;
//                    }
                    gameList.add(new DbGame(result.getInt(Constants.PLAYER1_ID),
                            result.getInt(Constants.PLAYER2_ID),
                            result.getInt(Constants.WINNER_ID),
                            result.getInt(Constants.ENDED) == 1,
                            result.getInt(Constants.ID)));
                }
            } catch (SQLException e) {
                System.err.println("Error parsing game list: " + e);
            }
        }

        return gameList;
    }

    public boolean deleteAllGames() {
        String sql = "DELETE FROM " + Constants.GAMES_TABLE + ";";

        return execute(sql);
    }

    //---------------------- OTHER METHODS --------------------
    private ResultSet executeQuery(String sql) {
        Statement statement = null;
        ResultSet result = null;
        if (connection == null)
            throw new NotConnectedException("Not connected to the database yet.");
        try {
            statement = connection.createStatement();
            result = statement.executeQuery(sql);
        } catch (SQLException e) {
            //e.printStackTrace();
            System.err.println("Error executing query:\n" + e);
            return null;
        }

        return result;
    }

    private boolean execute(String sql) {
        Statement statement = null;
        if (connection == null)
            throw new NotConnectedException("Not connected to the database yet.");
        try {
            statement = connection.createStatement();
            statement.execute(sql);

        } catch (SQLException e) {
            //e.printStackTrace();
            System.err.println("Error on execute: " + e);
            return false;
        }

        return true;
    }
}
