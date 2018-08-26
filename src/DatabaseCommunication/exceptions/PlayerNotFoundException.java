/**
 * Created by Lídia on 22/08/2018
 */

package DatabaseCommunication.exceptions;

public class PlayerNotFoundException extends Exception {

    public PlayerNotFoundException(int playerId){
        super("The player with id=" + playerId + " doesn't exist.");
    }

    public PlayerNotFoundException(String userName){
        super("The player with userName(or id)=" + userName + " doesn't exist.");
    }

    public PlayerNotFoundException(){
        super("The player doesn't exist.");
    }
}
