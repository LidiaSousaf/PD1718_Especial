/**
 * Created by Lídia on 25/08/2018
 */

package GameClient.gui;

import GameClient.GlobalController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RegisterPanel extends JPanel implements ActionListener {

    private GlobalController globalController;
    private JTextField tfUserName;
    private JLabel lbUserName;
    private JTextField tfName;
    private JLabel lbName;
    private JPasswordField tpPassword;
    private JLabel lbPassword;
    private JButton btRegister;
    private JLabel panelTitle;

    public RegisterPanel(GlobalController globalController){
        this.globalController = globalController;

        createComponents();

        setUpLayout();

        setEventListeners();

        validate();
    }

    private void createComponents(){
        panelTitle = new JLabel("Novo Registo");
        tfUserName = new JTextField(20);
        lbUserName = new JLabel("Username");
        tfName = new JTextField(20);
        lbName = new JLabel("Nome");
        tpPassword = new JPasswordField(20);
        lbPassword = new JLabel("Password");
        btRegister = new JButton("Registar");
    }

    private void setUpLayout(){

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));


        Font font = panelTitle.getFont().deriveFont(20.0f);
        panelTitle.setFont(font);
        panelTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(panelTitle);

        add(Box.createVerticalStrut(30));

        lbUserName.setAlignmentX(Component.CENTER_ALIGNMENT);
        tfUserName.setPreferredSize(new Dimension(120,20));
        tfUserName.setMaximumSize(new Dimension(200,40));
        tfUserName.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(lbUserName);
        add(Box.createVerticalStrut(5));
        add(tfUserName);

        add(Box.createVerticalStrut(20));

        lbName.setAlignmentX(Component.CENTER_ALIGNMENT);
        tfName.setPreferredSize(new Dimension(120,20));
        tfName.setMaximumSize(new Dimension(200,40));
        tfName.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(lbName);
        add(Box.createVerticalStrut(5));
        add(tfName);

        add(Box.createVerticalStrut(20));

        lbPassword.setAlignmentX(Component.CENTER_ALIGNMENT);
        tpPassword.setPreferredSize(new Dimension(120,20));
        tpPassword.setMaximumSize(new Dimension(200,40));
        tpPassword.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(lbPassword);
        add(Box.createVerticalStrut(5));
        add(tpPassword);

        add(Box.createVerticalStrut(50));

        btRegister.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(btRegister);
    }

    private void setEventListeners(){
        btRegister.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        globalController.registerNewPlayer(tfUserName.getText(), tfName.getText(), new String(tpPassword.getPassword()));
        tfUserName.setText("");
        tfName.setText("");
        tpPassword.setText("");
    }
}
