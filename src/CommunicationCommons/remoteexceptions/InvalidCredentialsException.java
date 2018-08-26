/**
 * Created by Lídia on 26/08/2018
 */

package CommunicationCommons.remoteexceptions;

public class InvalidCredentialsException extends Exception {

    public InvalidCredentialsException(){
        super();
    }

    public InvalidCredentialsException(String message){
        super(message);
    }
}
