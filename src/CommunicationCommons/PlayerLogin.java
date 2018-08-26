package CommunicationCommons;

import java.io.Serializable;

public class PlayerLogin implements Serializable {
    //------------------------ CONSTANTS ----------------------
    public static final Long serialVersionUID = 12L;

    //------------------------ VARIABLES ----------------------
    private String userName;
    private String password;
    private String ipAddress;

    //---------------------- CONSTRUCTORS ---------------------
    public PlayerLogin(String userName, String password, String ipAddress) {
        this.userName = userName;
        this.password = password;
        this.ipAddress = ipAddress;
    }

    //------------------- GETTERS / SETTERS -------------------
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    //--------------------- OTHER METHODS ---------------------
    public boolean isValid() {
        return (userName != null && !userName.isEmpty() && userName.length() <= 16)
                && (password != null && !password.isEmpty() && password.length() <= 16);
    }
}
