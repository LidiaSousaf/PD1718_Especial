/**
 * Created by Lídia on 27/08/2018
 */

package CommunicationCommons.remoteexceptions;

public class AlreadyPairedException extends Exception {
    public AlreadyPairedException() {
        super();
    }

    public AlreadyPairedException(String message) {
        super(message);
    }
}
