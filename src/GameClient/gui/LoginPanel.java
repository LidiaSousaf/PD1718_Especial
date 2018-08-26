/**
 * Created by Lídia on 25/08/2018
 */

package GameClient.gui;

import GameClient.GlobalController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginPanel extends JPanel implements ActionListener {

    private GlobalController globalController;
    private JTextField tfUserName;
    private JLabel lbUserName;
    private JPasswordField tpPassword;
    private JLabel lbPassword;
    private JButton btLogin;
    private JLabel panelTitle;

    public LoginPanel(GlobalController globalController){
        this.globalController = globalController;

        createComponents();

        setUpLayout();

        setEventListeners();

        validate();
    }

    private void createComponents(){
        panelTitle = new JLabel("Login");
        tfUserName = new JTextField(20);
        lbUserName = new JLabel("Username");
        tpPassword = new JPasswordField(20);
        lbPassword = new JLabel("Password");
        btLogin = new JButton("Login");
    }

    private void setUpLayout(){

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        Font font = panelTitle.getFont().deriveFont(20.0f);
        panelTitle.setFont(font);
        panelTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(panelTitle);

        add(Box.createVerticalStrut(50));

        lbUserName.setAlignmentX(Component.CENTER_ALIGNMENT);
        tfUserName.setPreferredSize(new Dimension(120,20));
        tfUserName.setMaximumSize(new Dimension(200,40));
        tfUserName.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(lbUserName);
        add(Box.createVerticalStrut(5));
        add(tfUserName);

        add(Box.createVerticalStrut(20));

        lbPassword.setAlignmentX(Component.CENTER_ALIGNMENT);
        tpPassword.setPreferredSize(new Dimension(120,20));
        tpPassword.setMaximumSize(new Dimension(200,40));
        tpPassword.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(lbPassword);
        add(Box.createVerticalStrut(5));
        add(tpPassword);

        add(Box.createVerticalStrut(50));

        btLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(btLogin);
    }

    private void setEventListeners(){
        btLogin.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        globalController.login(tfUserName.getText(), new String(tpPassword.getPassword()));
    }
}
