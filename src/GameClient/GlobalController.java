package GameClient;

import CommunicationCommons.*;
import CommunicationCommons.remoteexceptions.AlreadyExistsRemoteException;
import CommunicationCommons.remoteexceptions.AlreadyLoggedRemoteException;
import CommunicationCommons.remoteexceptions.InvalidCredentialsException;
import CommunicationCommons.remoteexceptions.NotLoggedException;

import javax.swing.*;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

public class GlobalController extends Observable {

    //----------------------------- VARIABLES -----------------------------
    private ClientManagementInterface clientManagement;
    private PlayerLogin login;
    private boolean hasRequestedPair;

    //---------------------------- CONSTRUCTOR ----------------------------
    public GlobalController(String managementAddress) {
        startManagementServerConnection(managementAddress);
        hasRequestedPair = false;
    }

    //------------------------- GETTERS / SETTERS -------------------------
    public PlayerLogin getLogin() {
        return login;
    }

    public void setLogin(PlayerLogin login) {
        this.login = login;
        setChanged();
        notifyObservers(login);
    }

    public boolean getHasRequestedPair() {
        return hasRequestedPair;
    }

    public void setHasRequestedPair(boolean hasRequestedPair) {
        this.hasRequestedPair = hasRequestedPair;
    }

    //---------------------- MANAGEMENT COMMUNICATION ---------------------
    private void startManagementServerConnection(String managementAddress) {
        try {
            String managementServiceURL = "rmi://" + managementAddress + "/ClientManagementService";
            clientManagement = (ClientManagementInterface) Naming.lookup(managementServiceURL);

        } catch (NotBoundException | MalformedURLException | RemoteException e) {
            JOptionPane.showMessageDialog(null, "Erro ao estabelecer comunicação com o servidor de gestão:\n" + e.getMessage());
            e.printStackTrace();
            shutdownClient(-1);
        }
    }

    public void login(String userName, String password) {
        String ip = null;

        try {
            ip = InetAddress.getLocalHost().getHostAddress();
            PlayerLogin loginAttempt = new PlayerLogin(userName, password, ip);

            if (!loginAttempt.isValid()) {
                JOptionPane.showMessageDialog(null, "Credenciais de login inválidas!");
                return;
            }

            if (clientManagement.login(loginAttempt)) {
                setLogin(loginAttempt);
                JOptionPane.showMessageDialog(null, "Login efetuado com sucesso.");
            }

        } catch (InvalidCredentialsException e) {
            JOptionPane.showMessageDialog(null, "Credenciais de login inválidas!");
        } catch (AlreadyLoggedRemoteException e) {
            JOptionPane.showMessageDialog(null, "O jogador já está logado!");
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(null, "Erro inesperado: " + e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        } catch (UnknownHostException e) {
            JOptionPane.showMessageDialog(null,
                    "Erro ao obter o endereço de rede do cliente: " + e.getMessage());
            shutdownClient(-1);
        }
    }

    public void registerClientCallback(RemoteClientInterface clientCallback) {
        try {
            clientManagement.registerRemoteClient(login.getUserName(), clientCallback);
        } catch (InvalidCredentialsException e) {
            JOptionPane.showMessageDialog(null, "O jogador não se encontra registado!");
        } catch (NotLoggedException e) {
            JOptionPane.showMessageDialog(null, "O jogador não se encontra logado!");
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(null, "Erro inesperado: " + e);
            shutdownClient(-1);
        }
    }

    private void logout() {
        //TODO: end game and logout from GameServer as well
        try {
            clientManagement.logout(login.getUserName());
        } catch (InvalidCredentialsException e) {
            System.err.println("Credenciais inválidas");
        } catch (RemoteException e) {
            System.err.println(e.getMessage());
        }
    }

    public void registerNewPlayer(String userName, String name, String password) {
        PlayerRegister register = new PlayerRegister(userName, name, password);
        try {
            if (clientManagement.registerPlayer(register)) {
                JOptionPane.showMessageDialog(null, "Registo realizado com sucesso.");
            }
        } catch (InvalidCredentialsException e) {
            JOptionPane.showMessageDialog(null, "Credenciais de registo inválidas!");
        } catch (AlreadyExistsRemoteException e) {
            JOptionPane.showMessageDialog(null, "O utilizador com Username " + userName + " já se encontra registado!");
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(null, "Erro inesperado: " + e.getMessage());
            e.printStackTrace();
            shutdownClient(-1);
        }
    }

    public List<LoggedPlayerInfo> getLoggedPlayers(){
        List<LoggedPlayerInfo> playerList = new ArrayList<>();
        try {
            playerList = clientManagement.getLoggedPlayers();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return playerList;
    }

    //-------------------------- OTHER METHODS ----------------------------
    public void shutdownClient(int exitStatus) {
        if (login != null) {
            logout();
        }

        System.exit(exitStatus);

    }
}
