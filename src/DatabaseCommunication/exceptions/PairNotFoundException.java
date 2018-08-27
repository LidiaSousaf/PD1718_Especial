/**
 * Created by Lídia on 27/08/2018
 */

package DatabaseCommunication.exceptions;

public class PairNotFoundException extends Exception {
    public PairNotFoundException(){

    }

    public PairNotFoundException(String message){
        super(message);
    }
}
