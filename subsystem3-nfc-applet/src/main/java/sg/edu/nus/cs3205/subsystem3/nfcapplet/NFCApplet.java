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
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

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
        NFCPanel mainPanel = new NFCPanel();
        mainPanel.setOpaque(true);
        this.setContentPane(mainPanel);

        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    public static class NFCPanel extends JPanel implements ActionListener {
        private static final long serialVersionUID = -4443552310769524926L;
        public static final String LOGIN = "LOGIN";
        private LoginPanel loginPanel;
        private RegistrationPanel registrationPanel;

        public NFCPanel() {
            super(new BorderLayout());

            this.loginPanel = new LoginPanel(this);
            this.loginPanel.setPreferredSize(new Dimension(500, 200));
            this.add(this.loginPanel, BorderLayout.CENTER);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();

            if (LOGIN.equals(command)) {
                this.remove(this.loginPanel);
                this.revalidate();
                this.registrationPanel = new RegistrationPanel(this);
                this.add(this.registrationPanel, BorderLayout.CENTER);
            }
        }
    }

    public static class RegistrationPanel extends JPanel implements ActionListener {
        private static final long serialVersionUID = -7314885421740232209L;
        protected static final String REGISTER = "REGISTER";
        private JTextField searchTextField;
        protected User[] users = new User[0];
        private JTable usersTable;
        private TableRowSorter<TableModel> sorter;
        private JLabel selectedLabel;
        private User selectedUser;

        public RegistrationPanel(ActionListener aL) {
            super(new BorderLayout());
            ServerConnector.getUsers(users -> {
                this.users = users.users;
                ((AbstractTableModel) usersTable.getModel()).fireTableDataChanged();
                selectedLabel.setText("No user selected");
            });
            JPanel searchPanel = new JPanel();
            JLabel searchLabel = new JLabel("Filter Name/Username");
            searchPanel.add(searchLabel);
            searchTextField = new JTextField(20);
            searchTextField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void removeUpdate(DocumentEvent arg0) {
                    newFilter();
                }

                @Override
                public void insertUpdate(DocumentEvent arg0) {
                    newFilter();
                }

                @Override
                public void changedUpdate(DocumentEvent arg0) {
                    newFilter();
                }
            });
            searchPanel.add(searchTextField);

            usersTable = new JTable(new AbstractTableModel() {
                private static final long serialVersionUID = 976597182178888195L;

                @Override
                public int getRowCount() {
                    return users.length;
                }

                @Override
                public int getColumnCount() {
                    return User.FIELDS.length;
                }

                @Override
                public String getColumnName(int col) {
                    return User.FIELDS[col];
                }

                @Override
                public Object getValueAt(int row, int col) {
                    return users[row].get(col);
                }
            });
            this.sorter = new TableRowSorter<TableModel>(usersTable.getModel());
            usersTable.setRowSorter(this.sorter);
            usersTable.setPreferredScrollableViewportSize(new Dimension(800, 70));
            usersTable.setFillsViewportHeight(true);
            usersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            usersTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent event) {
                    int viewRow = usersTable.getSelectedRow();
                    if (viewRow < 0) {
                        // Selection got filtered away.
                        selectedLabel.setText("No user selected");
                    } else {
                        selectedUser = users[usersTable.convertRowIndexToModel(viewRow)];
                        selectedLabel.setText(String.format("Selected username: %s", selectedUser.username));
                    }
                }
            });
            JScrollPane tableContainer = new JScrollPane(usersTable);

            JPanel selectedPanel = new JPanel(new GridLayout(1, 2));
            selectedLabel = new JLabel("Fetching users list");
            selectedPanel.add(selectedLabel);
            JButton selectedButton = new JButton("Generate and write NFC secret");
            selectedButton.setActionCommand(REGISTER);
            selectedButton.addActionListener(this);
            selectedPanel.add(selectedButton);

            this.add(searchPanel, BorderLayout.NORTH);
            this.add(tableContainer, BorderLayout.CENTER);
            this.add(selectedPanel, BorderLayout.SOUTH);
        }

        private void newFilter() {
            RowFilter<TableModel, Integer> rf = null;
            try {
                rf = RowFilter.regexFilter("(?i)" + searchTextField.getText(), 0, 1, 2);
            } catch (java.util.regex.PatternSyntaxException e) {
                return;
            }
            this.sorter.setRowFilter(rf);
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if (REGISTER.equals(actionEvent.getActionCommand())) {
                if (selectedUser != null) {
                    ServerConnector.generateNFCSecret(selectedUser.username, secret -> {
                        selectedLabel.setText("Secret generated, tap your NFC card");
                        NFCService.writeData(() -> selectedLabel.setText("Writing to NFC card"),
                                () -> selectedLabel.setText("NFC card written, remove it"),
                                selectedUser.username, secret);
                    });
                }
            }
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

            buttonLogin.setActionCommand(NFCPanel.LOGIN);
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
