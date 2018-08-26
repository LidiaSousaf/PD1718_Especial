package ManagementServer.clientcommunication; /**
 * Created by Lídia on 23/08/2018
 */

import CommunicationCommons.RemoteClientInterface;

public class LoggedClientReference {
    private RemoteClientInterface clientReference;
    private String userName;

    public LoggedClientReference(RemoteClientInterface clientReference, String userName) {
        this.clientReference = clientReference;
        this.userName = userName;
    }

    public RemoteClientInterface getClientReference() {
        return clientReference;
    }

    public void setClientReference(RemoteClientInterface clientReference) {
        this.clientReference = clientReference;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
