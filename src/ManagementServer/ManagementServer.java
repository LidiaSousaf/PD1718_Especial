package ManagementServer;

import DatabaseCommunication.DatabaseCommunication;
import ManagementServer.clientcommunication.ClientManagementService;

import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class ManagementServer {
    private final static String DATABASE_NAME = "three_in_row";
    private final static String DATABASE_USER_NAME = "ManagementServer";
    private final static String DATABASE_PASSWORD = "ThreeInRow_1718";
    private final static String DATABASE_ADDRESS = "localhost";

    private static String clientManagementURL;

    public static void main(String[] args) {
        //Get the Ip address for the database
        String databaseIpAddress = DATABASE_ADDRESS;
        if (args.length >= 1) {
            databaseIpAddress = args[0];
        }

        DatabaseCommunication databaseCommunication = new DatabaseCommunication(DATABASE_NAME,
                DATABASE_USER_NAME, DATABASE_PASSWORD, databaseIpAddress);

        if(!databaseCommunication.connect()){
            System.out.println("Connection to database failed. Shutting down Management Server");
            System.exit(-1);
        }

        try {
            ClientManagementService clientManagementService = startClientManagementService(databaseCommunication);

            System.out.println("Press \"Enter\" to exit.");
            System.in.read();

            unbindClientManagementService(clientManagementService);
        } catch (IOException e) {
            System.err.println("An error occurred:" + e);
            System.exit(-1);
        } finally {
            databaseCommunication.closeConnection();
        }
    }

    private static ClientManagementService startClientManagementService(DatabaseCommunication databaseCommunication) {
        ClientManagementService clientManagementService = null;
        try {
            //Load the service
            clientManagementService = new ClientManagementService(databaseCommunication);
            System.out.println("Client Management service loaded.");

            //Start the registry on default port 1099.
            Registry registry;
            try {
                registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
            } catch (Exception e) {
                registry = LocateRegistry.getRegistry();
                System.out.println("Registry is already in use on port " + Registry.REGISTRY_PORT);
            }

            //Register the service
//            clientManagementURL = "rmi://" + InetAddress.getLocalHost().getHostAddress() + "/ClientManagementService";
//            Naming.bind(clientManagementURL, clientManagementService);
            registry.bind("ClientManagementService", clientManagementService);
            System.out.println("Client Management service registered (ClientManagementService).");

        } catch (AlreadyBoundException | IOException e) {
            System.err.println("An error occurred:" + e);
            System.exit(-1);
        }

        return clientManagementService;
    }

    private static void unbindClientManagementService(ClientManagementService clientManagementService){

        clientManagementService.logoutAllPlayers();
        try {
            //Remove from registry the reference to the service
//            Naming.unbind(clientManagementURL);
            //End the service
            UnicastRemoteObject.unexportObject(clientManagementService, true);
        } catch (RemoteException /*| NotBoundException | MalformedURLException*/ e) {
            System.err.println("An error occurred:" + e);
            System.exit(-1);
        }
    }
}
