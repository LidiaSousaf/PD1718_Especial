/**
 * Created by Lídia on 22/08/2018
 */

package DatabaseCommunication.exceptions;

public class NotConnectedException extends RuntimeException {

    public NotConnectedException(String message) {
        super(message);
    }

    public NotConnectedException() {

    }
}
