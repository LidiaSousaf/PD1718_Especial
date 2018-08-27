package GameClient; /**
 * Created by Lídia on 23/08/2018
 */

import GameClient.gui.ClientWindow;

import javax.swing.*;

public class GameClient {

    public static void main(String[] args) {
        if (args.length < 1) {
            JOptionPane.showMessageDialog(null, "Erro de sintaxe: java GameClient <ip_serv_gestao>");
            System.out.println("Erro de sintaxe: java GameClient <ip_serv_gestao>");
            System.exit(0);
        }

        String managementAddress = args[0];

        GlobalController globalController = null;
        try {
            globalController = new GlobalController(managementAddress);
            ClientWindow view = new ClientWindow(globalController);

        } finally {
//            if(globalController != null){
//                globalController.shutdownClient(0);
//            }
        }
    }

}
