/**
 * Created by Lídia on 30/08/2018
 */

package CommunicationCommons;

public class GameCommConstants {

    //Constants used in the communication between GameServer and GameClient

    public static final int SOCKET_TIMEOUT = 10000;

    public static final Integer CONNECTION_REFUSED = -1;
    public static final Integer CONNECTION_ACCEPTED = 1;

    public static final Integer MAKE_MOVE = 2;
    public static final Integer INTERRUPT = 3;
    public static final Integer GIVE_UP = 4;
}
