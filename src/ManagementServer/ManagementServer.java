package ManagementServer;

import DatabaseCommunication.DatabaseCommunication;
import ManagementServer.clientcommunication.ClientManagementService;
import ManagementServer.gameservercommunication.HeartbeatService;
import ManagementServer.gameservercommunication.HeartbeatServiceHandler;

import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class ManagementServer {
    private static final String DATABASE_NAME = "three_in_row";
    private static final String DATABASE_USER_NAME = "ManagementServer";
    private static final String DATABASE_PASSWORD = "ThreeInRow_1718";
    private static final String DATABASE_ADDRESS = "localhost";

    public static void main(String[] args) {
//        String databaseIpAddress = DATABASE_ADDRESS;
//        if (args.length >= 1) {
//            databaseIpAddress = args[0];
//        }

        //Get the Ip address for the database
        String databaseIpAddress = null;

        if (args.length >= 1) {
            databaseIpAddress = args[0];
        } else {
            Scanner sc = new Scanner(System.in);

            while (databaseIpAddress == null || databaseIpAddress.isEmpty()) {
                System.out.println("Insert the IP address of the Database Server:");
                databaseIpAddress = sc.nextLine();
            }
        }

        DatabaseCommunication databaseCommunication = new DatabaseCommunication(DATABASE_NAME,
                DATABASE_USER_NAME, DATABASE_PASSWORD, databaseIpAddress);

        if (!databaseCommunication.connect()) {
            System.out.println("Connection to database failed. Shutting down Management Server");
            System.exit(-1);
        }

        try {
            HeartbeatService heartbeatService = startHeartbeatService(databaseIpAddress);
            HeartbeatServiceHandler heartbeatHandler = new HeartbeatServiceHandler(heartbeatService);
            heartbeatHandler.start();
            ClientManagementService clientManagementService = startClientManagementService(databaseCommunication,
                    heartbeatService);

            System.out.println("Press \"Enter\" to exit.");
            System.in.read();

            heartbeatHandler.setStop(true);
            unbindClientManagementService(clientManagementService);
        } catch (IOException e) {
            System.err.println("An error occurred:" + e);
            System.exit(-1);
        } finally {
            databaseCommunication.closeConnection();
            System.out.println("Database connection ended.");
        }
    }

    private static ClientManagementService startClientManagementService(DatabaseCommunication databaseCommunication,
                                                                        HeartbeatService heartbeatService) {
        ClientManagementService clientManagementService = null;
        try {
            //Load the service
            clientManagementService = new ClientManagementService(databaseCommunication, heartbeatService);
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
            registry.bind("ClientManagementService", clientManagementService);
            System.out.println("Client Management service registered (ClientManagementService).");

        } catch (AlreadyBoundException | IOException e) {
            System.err.println("An error occurred:" + e);
            System.exit(-1);
        }

        return clientManagementService;
    }

    private static void unbindClientManagementService(ClientManagementService clientManagementService) {

        clientManagementService.logoutAllPlayers();
        clientManagementService.deleteAllPairs();

        try {
            //End the service
            UnicastRemoteObject.unexportObject(clientManagementService, true);
            System.out.println("ClientManagement service ended.");
        } catch (RemoteException e) {
            System.err.println("An error occurred:" + e);
            System.exit(-1);
        }
    }

    private static HeartbeatService startHeartbeatService(String databaseIp) {
        HeartbeatService heartbeatService = null;
        try {
            //Load the service
            heartbeatService = new HeartbeatService(databaseIp);
            System.out.println("Heartbeat service loaded.");

            //Start the registry on default port 1099.
            Registry registry;
            try {
                registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
            } catch (Exception e) {
                registry = LocateRegistry.getRegistry();
                System.out.println("Registry is already in use on port " + Registry.REGISTRY_PORT);
            }

            //Register the service
            registry.bind("HeartbeatService", heartbeatService);
            System.out.println("Heartbeat service registered (HeartbeatService).");

        } catch (AlreadyBoundException | IOException e) {
            System.err.println("An error occurred:" + e);
            System.exit(-1);
        }

        return heartbeatService;
    }
}
