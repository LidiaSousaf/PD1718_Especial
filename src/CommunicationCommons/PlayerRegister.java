package CommunicationCommons;

import java.io.Serializable;

public class PlayerRegister implements Serializable {

    public static final Long serialVersionUID = 1L;

    private String userName;
    private String name;
    private String password;

    public PlayerRegister(String userName, String name, String password) {
        this.userName = userName;
        this.name = name;
        this.password = password;
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

    public boolean isValid(){
        return (userName != null && !userName.isEmpty() && userName.length() <= 16)
                && (name != null && !name.isEmpty() && name.length() <= 32)
                && (password != null && !password.isEmpty() && password.length() <= 16);
    }
}
