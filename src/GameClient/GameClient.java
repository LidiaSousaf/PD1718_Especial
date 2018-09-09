package GameClient; /**
 * Created by Lídia on 23/08/2018
 */

import GameClient.gui.ClientWindow;

import javax.swing.*;
import java.util.Scanner;

public class GameClient {

    public static void main(String[] args) {
//        if (args.length < 1) {
//            JOptionPane.showMessageDialog(null, "Erro de sintaxe: java GameClient <ip_serv_gestao>");
//            System.out.println("Erro de sintaxe: java GameClient <ip_serv_gestao>");
//            System.exit(0);
//        }
//
//        String managementAddress = args[0];

        String managementAddress = null;

        if (args.length < 1) {
            Scanner sc = new Scanner(System.in);

            while (managementAddress == null || managementAddress.isEmpty()) {
                System.out.println("Indique o endereço IP do Servidor de Gestão:");
                managementAddress = sc.nextLine();
            }
        } else {
            managementAddress = args[0];
        }

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
