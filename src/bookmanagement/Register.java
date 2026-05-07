package bookmanagement;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Register a new user into tblusers (no duplicate usernames).
 */
public class Register extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmPassword;
    private JButton btnSubmit;
    private JButton btnClear;

    public Register() {
        initComponents();
        wireEvents();
    }

    private void initComponents() {
        setTitle("Book Management — Register");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(420, 260);
        setLocationRelativeTo(null);

        txtUsername = new JTextField(15);
        txtPassword = new JPasswordField(15);
        txtConfirmPassword = new JPasswordField(15);
        btnSubmit = new JButton("Submit");
        btnClear = new JButton("Clear");

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));
        form.add(new JLabel("Username:"));
        form.add(txtUsername);
        form.add(new JLabel("Password:"));
        form.add(txtPassword);
        form.add(new JLabel("Confirm password:"));
        form.add(txtConfirmPassword);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        buttons.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        buttons.add(btnSubmit);
        buttons.add(btnClear);

        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }

    private void wireEvents() {
        btnSubmit.addActionListener(e -> handleSubmit());
        btnClear.addActionListener(e -> {
            txtUsername.setText("");
            txtPassword.setText("");
            txtConfirmPassword.setText("");
        });
    }

    private void handleSubmit() {
        String user = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword());
        String confirm = new String(txtConfirmPassword.getPassword());

        if (user.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!pass.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Password and confirm password do not match.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            if (usernameExists(conn, user)) {
                JOptionPane.showMessageDialog(this, "Username already exists. Choose another.",
                        "Duplicate username", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String insert = "INSERT INTO tblusers (username, password) VALUES (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insert)) {
                ps.setString(1, user);
                ps.setString(2, pass);
                ps.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "Registration successful. Please log in.",
                    "Done", JOptionPane.INFORMATION_MESSAGE);

            Login login = new Login();
            login.setVisible(true);
            dispose();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean usernameExists(Connection conn, String username) throws SQLException {
        String sql = "SELECT userid FROM tblusers WHERE username = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
