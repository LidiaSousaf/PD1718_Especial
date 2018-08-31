/**
 * Created by Lídia on 22/08/2018
 */

package DatabaseCommunication.models;

public class DbPlayer {

    //------------------------ CONSTANTS ----------------------
    public static final int INVALID_ID = -1;

    //------------------------ VARIABLES ----------------------
    private int id;
    private String userName;
    private String name;
    private String password;
    private String ipAddress;
    private boolean logged;

    //---------------------- CONSTRUCTORS ---------------------
    public DbPlayer(String userName, String name, String password, String ipAddress, int id, boolean logged) {
        this.userName = userName;
        this.name = name;
        this.password = password;
        this.ipAddress = ipAddress;
        this.id = id;
        this.logged = logged;
    }

    public DbPlayer(String userName, String name, String password, String ipAddress) {
        this(userName, name, password, ipAddress, INVALID_ID, false);
    }

    public DbPlayer(String userName, String name, String password) {
        this(userName, name, password, "", INVALID_ID, false);
    }

    //------------------- GETTERS / SETTERS -------------------
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public boolean isLogged() {
        return logged;
    }

    public void setLogged(boolean logged) {
        this.logged = logged;
    }

    //--------------------- OTHER METHODS ---------------------
    public boolean isValid() {
        return (userName != null && !userName.isEmpty() && userName.length() <= 16)
                && (name != null && !name.isEmpty() && name.length() <= 32)
                && (password != null && !password.isEmpty() && password.length() <= 16);
    }

    @Override
    public String toString() {
        return "Id: " + id + " Nome: " + name + " UserName: " + userName + " Password: " + password + " Logado: "
                + logged + " Address: " + ipAddress;
    }
}
