/**
 * Created by Lídia on 26/08/2018
 */

package CommunicationCommons;

import java.io.Serializable;

public class LoggedPlayerInfo implements Serializable {
    //------------------------ CONSTANTS ----------------------
    public static final Long serialVersionUID = 3L;

    //------------------------ VARIABLES ----------------------
    private String userName;
    private String name;
    private boolean hasPair;

    //---------------------- CONSTRUCTORS ---------------------
    public LoggedPlayerInfo(String userName, String name, boolean hasPair) {
        this.userName = userName;
        this.name = name;
        this.hasPair = hasPair;
    }

    //------------------- GETTERS / SETTERS -------------------
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

    public boolean getHasPair() {
        return hasPair;
    }

    public void setHasPair(boolean hasPair) {
        this.hasPair = hasPair;
    }
}
