import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
// Laszlo Botond (lbim2260) - 522/2


public class Kliens extends JFrame {
    private Socket sendSocket;
    private ServerUpdateListener serverUpdater;
    private Thread updaterThread;
    private JTextField textField;
    private JLabel membersText;
    private JTextArea chatArea;
    private String username;
    private boolean givenUsername = false;
    public Kliens() {
        createWindow();
    }

    private void connectToServer() {
        try {
            sendSocket = new Socket("localhost", 22600);

            serverUpdater = new ServerUpdateListener(this, sendSocket);
            updaterThread = new Thread(serverUpdater);
            updaterThread.start();

            PrintStream socketOutput = new PrintStream(sendSocket.getOutputStream());
            String message = "#name:" + username + "\n";
            socketOutput.println(message);
        } catch (IOException e) {
            chatArea.setText("Error: Server is not running!");
            chatArea.setForeground(new Color(255, 0, 0));
            remove(textField);
            revalidate();
        }
    }

    private void sendMessage() {
        // keres kuldese
        try {
            PrintStream socketOutput = new PrintStream(sendSocket.getOutputStream());
            String message = textField.getText();
            String target;
            if (message.startsWith("#private#") && message.split("#").length > 3) {
                try {
                    target = message.split("#")[2];
                } catch (IndexOutOfBoundsException e) {
                    target = "";
                }
                String initPart = "#private#" + target + "#";
                message = initPart + textField.getText().substring(initPart.length());
            } else if (!message.startsWith("#users")) {
                message = username + ":" + textField.getText();
            }
            socketOutput.println(message);
        } catch (IOException e) {
            chatArea.setText(chatArea.getText() + "Error: Couldn't send message!\n");
        }
    }

    private void createWindow() {
        // chat text field
        textField = new JTextField();
        add(textField, BorderLayout.SOUTH);
        textField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (textField.getText().isEmpty()) {
                    return;
                }
                if (!givenUsername) { // has to enter username first
                    if (textField.getText().contains("#") || textField.getText().contains(" ") || textField.getText().contains(":")) {
                        textField.setText(textField.getText()
                                .replace('#', '_')
                                .replace(' ', '_')
                                .replace(':', '_'));
                        return;
                    }
                    givenUsername = true;
                    username = textField.getText();
                    chatArea.setText("Welcome to the chatroom!\n" +
                            "Please enter your name in chat! It can not contain '#', ':' or ' '.\n" +
                            "Welcome, " + username + "!\n");
                    connectToServer();
                } else {
                    sendMessage();
                }
                textField.setText("");
            }
        });
        // participant list
        JPanel memberPanel = new JPanel();
        JScrollPane memberScrollPane = new JScrollPane(memberPanel);
        membersText = new JLabel("<html><h4>Member list:</h4></html>");
        memberScrollPane.setPreferredSize(new Dimension(100, 500));
        memberPanel.add(membersText);
        add(memberScrollPane, BorderLayout.WEST);
        // chat panel
        chatArea = new JTextArea();
        chatArea.setText("""
                Welcome to the chatroom!
                Please enter your name in chat! It can not contain '#', ':' or ' '.
                """);
        chatArea.setEditable(false);
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        chatScrollPane.setPreferredSize(new Dimension(400,400));
        add(chatScrollPane);

        setTitle("Chat Client");
        setBounds(100,100,500,500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    public void addToChat(String s) {
        if (s.isEmpty()) return;
        if (!s.endsWith("\n")) {
            s = s + "\n";
        }
        chatArea.setText(chatArea.getText() + s);
    }

    public void refreshNames(String s) {
        String newText = "<html><h4>Member list:</h4>";
        String[] names = s.split(":");
        for (int i=1; i<names.length; i++) {
            System.out.println(names[i]);
            newText = newText + names[i] + "<br>";
        }
        newText = newText + "</html>";
        membersText.setText(newText);
    }

    public void requestNewName() throws IOException {
        givenUsername = false;
        addToChat("Name taken! Please enter new name in chat!");
        sendSocket.close();
        updaterThread.interrupt();
    }

    public static void main(String[] args) {
        new Kliens();
    }
}