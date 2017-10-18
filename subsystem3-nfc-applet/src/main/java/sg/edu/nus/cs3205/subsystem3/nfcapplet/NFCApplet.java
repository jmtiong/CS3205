package sg.edu.nus.cs3205.subsystem3.nfcapplet;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class NFCApplet extends JFrame {
    private static final long serialVersionUID = 3899822683922114716L;

    public static void main(String... args) throws Exception {
        NFCApplet app = new NFCApplet();
        app.start();
    }

    public NFCApplet() {
        super("NFC Registration");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void start() {
        NFCPanel newContentPane = new NFCPanel();
        newContentPane.setOpaque(true);
        this.setContentPane(newContentPane);

        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    public static class NFCPanel extends JPanel implements ActionListener {
        private static final long serialVersionUID = -4443552310769524926L;
        private LoginPanel loginPanel;
        private RegistrationPanel nfcPanel;

        public NFCPanel() {
            super(new BorderLayout());

            this.loginPanel = new LoginPanel(this);
            this.loginPanel.setPreferredSize(new Dimension(500, 200));
            this.add(this.loginPanel, BorderLayout.CENTER);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();

            if ("LOGIN".equals(command)) {
                remove(this.loginPanel);
                revalidate();
                this.nfcPanel = new RegistrationPanel(this);
                this.add(this.nfcPanel, BorderLayout.CENTER);
            } else if ("READ".equalsIgnoreCase(command)) {
                NFCService.readData(
                        data -> this.nfcPanel.textRead.setText(String.join(System.lineSeparator(), data)));
            } else if ("WRITE".equalsIgnoreCase(command)) {
                NFCService.writeData(this.nfcPanel.textWrite.getText());
            } else {
                this.nfcPanel.textRead.setText(this.nfcPanel.textWrite.getText());
            }
        }
    }

    public static class RegistrationPanel extends JPanel {
        private static final long serialVersionUID = -7314885421740232209L;
        public JTextArea textRead = new JTextArea("");
        private JButton buttonRead = new JButton("Read");
        private JTextField textWrite = new JTextField(20);
        private JButton buttonWrite = new JButton("Write");

        public RegistrationPanel(ActionListener aL) {
            super(new GridLayout(2, 0));
            textRead.setEditable(false);
            buttonRead.addActionListener(aL);
            buttonWrite.addActionListener(aL);
            this.add(textRead);
            this.add(buttonRead);
            this.add(textWrite);
            this.add(buttonWrite);
        }
    }

    public static class LoginPanel extends JPanel {
        private static final long serialVersionUID = -4785045872369902470L;
        private JLabel labelUsername = new JLabel("Enter username: ");
        private JLabel labelPassword = new JLabel("Enter password: ");
        private JTextField textUsername = new JTextField(20);
        private JPasswordField fieldPassword = new JPasswordField(20);
        private JButton buttonLogin = new JButton("Login");

        public LoginPanel(ActionListener aL) {
            super(new GridBagLayout());

            GridBagConstraints constraints = new GridBagConstraints();
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = new Insets(10, 10, 10, 10);

            // add components to the panel
            constraints.gridx = 0;
            constraints.gridy = 0;
            add(labelUsername, constraints);

            constraints.gridx = 1;
            add(textUsername, constraints);

            constraints.gridx = 0;
            constraints.gridy = 1;
            add(labelPassword, constraints);

            constraints.gridx = 1;
            add(fieldPassword, constraints);

            buttonLogin.setActionCommand("LOGIN");
            buttonLogin.addActionListener(aL);

            constraints.gridx = 0;
            constraints.gridy = 2;
            constraints.gridwidth = 2;
            constraints.anchor = GridBagConstraints.CENTER;
            add(buttonLogin, constraints);

            // set border for the panel
            setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Staff Login"));
        }
    }
}
