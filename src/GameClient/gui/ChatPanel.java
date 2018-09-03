/**
 * Created by Lídia on 02/09/2018
 */

package GameClient.gui;

import GameClient.GlobalController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChatPanel extends JPanel {
    private GlobalController controller;
    private JTextArea textArea;
    private JTextField targetTextField;
    private JTextField messageTextField;
    private JButton sendButton;
    private JLabel panelTitle;

    public ChatPanel(GlobalController controller) {
        this.controller = controller;

        createComponents();
        setUpLayout();
    }

    private void createComponents() {
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setPreferredSize(new Dimension(290, 100));
        textArea.setMaximumSize(new Dimension(290, 100));
        textArea.setLineWrap(true);

        targetTextField = new JTextField();
        targetTextField.setPreferredSize(new Dimension(100, 25));
        targetTextField.setMaximumSize(new Dimension(200, 30));

        messageTextField = new JTextField();
        messageTextField.setPreferredSize(new Dimension(100, 25));
        messageTextField.setMaximumSize(new Dimension(200, 30));

        sendButton = new JButton("Enviar");
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
                messageTextField.setText("");
            }
        });

        panelTitle = new JLabel("Chat");
        panelTitle.setFont(panelTitle.getFont().deriveFont(14.0f));
    }

    private void setUpLayout() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        panelTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(panelTitle);

        add(Box.createRigidArea(new Dimension(10, 10)));

        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        textArea.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(scroll);

        add(Box.createRigidArea(new Dimension(10, 10)));

        Box box1 = Box.createHorizontalBox();
        box1.add(new JLabel("Para: "));
        box1.add(Box.createRigidArea(new Dimension(10, 10)));
        box1.add(targetTextField);
        box1.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(box1);

        add(Box.createRigidArea(new Dimension(10, 10)));

        Box box2 = Box.createHorizontalBox();
        box2.add(new JLabel("Mensagem: "));
        box2.add(Box.createRigidArea(new Dimension(10, 10)));
        box2.add(messageTextField);
        box2.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(box2);

        add(Box.createRigidArea(new Dimension(10, 10)));
        Box box3 = Box.createHorizontalBox();
        sendButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        box3.add(sendButton);
        box3.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(box3);
        add(Box.createVerticalStrut(10));

        setVisible(true);
        validate();
    }

    private void sendMessage() {
        String target = targetTextField.getText();
        String message = messageTextField.getText();
        if (target != null && target.length() > 0
                && message != null && message.length() > 0) {
            controller.sendMessage(target, message);
        }
    }

    public void receiveMessage(String sender, String target, String message) {
        String text = "De " + sender + " para " + target + ":\n" + message + "\n\n";
        textArea.append(text);
    }
}
