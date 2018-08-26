/**
 * Created by Lídia on 22/08/2018
 */

package DatabaseCommunication;

import DatabaseCommunication.models.Pair;
import DatabaseCommunication.models.Player;
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
        try {
            connection = DriverManager.getConnection("jdbc:mysql://" + dbIpAddress + ":" + dbPort + "/"
                    + databaseName + Constants.CONNECTION_STRING_END, dbUserName, dbPassword);
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
    public Player getPlayerByUserName(String userName) throws PlayerNotFoundException {
        String query = "SELECT * FROM " + Constants.PLAYERS_TABLE + " WHERE " + Constants.USERNAME + " = '" + userName + "';";

        return parsePlayer(query, userName);
    }

    public Player getPlayerById(int id) throws PlayerNotFoundException {
        if (id < 1) {
            throw new PlayerNotFoundException(id);
        }
        String query = "SELECT * FROM " + Constants.PLAYERS_TABLE + " WHERE " + Constants.ID + " = '" + id + "';";

        return parsePlayer(query, "" + id);
    }

    public Player parsePlayer(String query, String userName) throws PlayerNotFoundException {
        ResultSet result = executeQuery(query);

        if (result != null) {
            try {
                if (result.next()) {
                    String ip = result.getString(Constants.IP_ADDRESS);
                    if (result.wasNull()) {
                        ip = "";
                    }

                    return new Player(
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

    public boolean registerPlayer(Player player) throws InvalidPlayerException, PlayerAlreadyExistsException {
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

    public boolean login(Player player) throws PlayerNotFoundException, AlreadyLoggedInException {

        Player playerInDb = getPlayerByUserName(player.getUserName());

        if (playerInDb.isLogged()) {
            throw new AlreadyLoggedInException();
        }

//        System.out.println("playerInDbPassword: " + playerInDb.getPassword());
//        System.out.println("playerPassword: " + player.getPassword());

        if (playerInDb.getPassword().equals(player.getPassword())) {

            String sql = "UPDATE " + Constants.PLAYERS_TABLE + " SET " + Constants.LOGGED + " = '1', "
                    + Constants.IP_ADDRESS + " = '" + player.getIpAddress()
                    + "' WHERE " + Constants.USERNAME + " = '" + player.getUserName() + "';";

            boolean result = execute(sql);
            if (result) {
                player.setLogged(true);
            }

            return result;

        } else {
            throw new PlayerNotFoundException();
        }
    }

    public boolean logout(Player player) throws PlayerNotFoundException {
        if (player == null) {
            throw new PlayerNotFoundException();
        }

        getPlayerByUserName(player.getUserName());

        String sql = "UPDATE " + Constants.PLAYERS_TABLE + " SET " + Constants.LOGGED + " = '0', "
                + Constants.IP_ADDRESS + " = " + "NULL"
                + " WHERE " + Constants.USERNAME + " = '"
                + player.getUserName() + "';";

        boolean result = execute(sql);
        if (result)
            player.setLogged(false);

        return result;
    }

    public void logoutAllPlayers() {
        String sql = "UPDATE " + Constants.PLAYERS_TABLE + " SET "
                + Constants.LOGGED + " = '0', "
                + Constants.IP_ADDRESS + " = " + "NULL"
                + " WHERE " + Constants.LOGGED + " != '0';";

        execute(sql);
    }

    public List<Player> getLoggedPlayers() {
        String query = "SELECT * FROM " + Constants.PLAYERS_TABLE + " WHERE " + Constants.LOGGED + " = '1';";
        ResultSet result = executeQuery(query);
        return parsePlayerList(result);
    }

    private List<Player> parsePlayerList(ResultSet result) {
        List<Player> playerList = new ArrayList<>();

        if (result != null) {
            try {
                while (result.next()) {
                    String ip = result.getString(Constants.IP_ADDRESS);
                    if (result.wasNull())
                        ip = "";
                    playerList.add(new Player(result.getString(Constants.USERNAME), result.getString(Constants.NAME),
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
    public boolean checkIfPlayerHasPair(Player player) {
        String query = "SELECT * FROM " + Constants.PAIRS_TABLE + " WHERE " +
                Constants.PLAYER1_ID + " = '" + player.getId() + "' OR " +
                Constants.PLAYER2_ID + " = '" + player.getId() + "';";
        ResultSet result = executeQuery(query);

        return parsePairList(result).size() > 0;
    }

    private List<Pair> parsePairList(ResultSet result) {
        List<Pair> pairList = new ArrayList<>();

        if (result != null) {
            try {
                while (result.next()) {
                    pairList.add(new Pair(result.getInt(Constants.PLAYER1_ID),
                            result.getInt(Constants.PLAYER2_ID),
                            result.getInt(Constants.FORMED) == 1));
                }
            } catch (SQLException e) {
                System.err.println("Error parsing player list: " + e);
            }
        }

        return pairList;
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
            throw new NotConnectedException("Ainda não foi estabelecida a comunicação com a base de dados");
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
