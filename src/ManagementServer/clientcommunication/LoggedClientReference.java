package ManagementServer.clientcommunication; /**
 * Created by Lídia on 23/08/2018
 */

import CommunicationCommons.RemoteClientInterface;

public class LoggedClientReference {
    private RemoteClientInterface clientCallback;
    private String userName;

    public LoggedClientReference(RemoteClientInterface clientCallback, String userName) {
        this.clientCallback = clientCallback;
        this.userName = userName;
    }

    public RemoteClientInterface getClientCallback() {
        return clientCallback;
    }

    public void setClientCallback(RemoteClientInterface clientCallback) {
        this.clientCallback = clientCallback;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
