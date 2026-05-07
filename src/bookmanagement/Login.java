package bookmanagement;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Login screen — checks tblusers with PreparedStatement.
 * <p>
 * NetBeans GUI Builder: JFrame form, place components and set variable names
 * to match the fields below, then move the listener code into the generated handlers.
 */
public class Login extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnClear;
    private JButton btnRegister;

    public Login() {
        initComponents();
        wireEvents();
    }

    private void initComponents() {
        setTitle("Book Management — Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 220);
        setLocationRelativeTo(null);

        txtUsername = new JTextField(15);
        txtPassword = new JPasswordField(15);
        btnLogin = new JButton("Login");
        btnClear = new JButton("Clear");
        btnRegister = new JButton("Register");

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));
        form.add(new JLabel("Username:"));
        form.add(txtUsername);
        form.add(new JLabel("Password:"));
        form.add(txtPassword);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        buttons.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        buttons.add(btnLogin);
        buttons.add(btnClear);
        buttons.add(btnRegister);

        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }

    private void wireEvents() {
        btnLogin.addActionListener(e -> handleLogin());
        btnClear.addActionListener(e -> {
            txtUsername.setText("");
            txtPassword.setText("");
        });
        btnRegister.addActionListener(e -> openRegister());
    }

    private void handleLogin() {
        String user = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "SELECT userid FROM tblusers WHERE username = ? AND password = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user);
            ps.setString(2, pass);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, "Login successful.",
                            "Welcome", JOptionPane.INFORMATION_MESSAGE);
                    openBookRegistration();
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid username or password.",
                            "Login failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openBookRegistration() {
        BookRegistration reg = new BookRegistration();
        reg.setVisible(true);
        dispose();
    }

    private void openRegister() {
        Register r = new Register();
        r.setVisible(true);
        dispose();
    }
}
