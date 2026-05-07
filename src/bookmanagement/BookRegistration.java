package bookmanagement;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class BookRegistration extends JFrame implements BookOperations {

    private JTextField txtTitle;
    private JTextField txtAuthor;
    private JTextField txtCategory;
    private JTextField txtStatus;
    private JTextField txtSearch;
    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnClear;
    private JButton btnRefresh;
    private JTable tblBooks;

    /** Set when a table row is selected; used for update/delete (bookid is auto-increment in DB). */
    private int selectedBookId = -1;

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Book ID", "Title", "Author", "Category", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public BookRegistration() {
        initComponents();
        wireEvents();
        loadBooks();
    }

    private void initComponents() {
        setTitle("Book Management — Books");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 520);
        setLocationRelativeTo(null);

        txtTitle = new JTextField(12);
        txtAuthor = new JTextField(12);
        txtCategory = new JTextField(12);
        txtStatus = new JTextField(12);
        txtSearch = new JTextField(20);

        btnAdd = new JButton("ADD");
        btnUpdate = new JButton("UPDATE");
        btnDelete = new JButton("DELETE");
        btnClear = new JButton("CLEAR");
        btnRefresh = new JButton("REFRESH");

        tblBooks = new JTable(tableModel);
        tblBooks.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblBooks.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search title:"));
        searchPanel.add(txtSearch);

        JPanel fields = new JPanel(new GridLayout(0, 2, 6, 6));
        fields.setBorder(BorderFactory.createTitledBorder("Book details"));
        fields.add(new JLabel("Title:"));
        fields.add(txtTitle);
        fields.add(new JLabel("Author:"));
        fields.add(txtAuthor);
        fields.add(new JLabel("Category:"));
        fields.add(txtCategory);
        fields.add(new JLabel("Status:"));
        fields.add(txtStatus);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.add(btnAdd);
        buttons.add(btnUpdate);
        buttons.add(btnDelete);
        buttons.add(btnClear);
        buttons.add(btnRefresh);

        JPanel south = new JPanel(new BorderLayout(0, 8));
        south.add(fields, BorderLayout.CENTER);
        south.add(buttons, BorderLayout.SOUTH);

        add(searchPanel, BorderLayout.NORTH);
        add(new JScrollPane(tblBooks), BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
    }

    private void wireEvents() {
        btnAdd.addActionListener(e -> addBook());
        btnUpdate.addActionListener(e -> updateBook());
        btnDelete.addActionListener(e -> deleteBook());
        btnClear.addActionListener(e -> clearFields());
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            loadBooks();
        });

        tblBooks.getSelectionModel().addListSelectionListener(this::onRowSelected);

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applySearchFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applySearchFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applySearchFilter();
            }
        });
    }

    /**
     * Live search while typing — uses LIKE %value%.
     */
    private void applySearchFilter() {
        try {
            List<LibraryBook> books = fetchBooks(txtSearch.getText());
            fillTable(books);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Search error:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onRowSelected(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
            return;
        }
        int row = tblBooks.getSelectedRow();
        if (row < 0) {
            selectedBookId = -1;
            return;
        }
        Object idCell = tableModel.getValueAt(row, 0);
        selectedBookId = idCell instanceof Number ? ((Number) idCell).intValue()
                : Integer.parseInt(String.valueOf(idCell));
        txtTitle.setText(String.valueOf(tableModel.getValueAt(row, 1)));
        txtAuthor.setText(String.valueOf(tableModel.getValueAt(row, 2)));
        txtCategory.setText(String.valueOf(tableModel.getValueAt(row, 3)));
        txtStatus.setText(String.valueOf(tableModel.getValueAt(row, 4)));
    }

    /** Loads rows from DB using LibraryBook (subclass of Book). */
    private List<LibraryBook> fetchBooks(String titleFilter) throws SQLException {
        List<LibraryBook> list = new ArrayList<>();
        String sql = "SELECT bookid, title, author, category, status FROM tblbooks WHERE title LIKE ? ORDER BY bookid";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String pattern = "%" + (titleFilter == null ? "" : titleFilter) + "%";
            ps.setString(1, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LibraryBook b = new LibraryBook(
                            rs.getInt("bookid"),
                            rs.getString("title"),
                            rs.getString("author"),
                            rs.getString("category"),
                            rs.getString("status"));
                    list.add(b);
                }
            }
        }
        return list;
    }

    private void fillTable(List<LibraryBook> books) {
        tableModel.setRowCount(0);
        for (LibraryBook b : books) {
            tableModel.addRow(new Object[]{
                    b.getBookId(),
                    b.getTitle(),
                    b.getAuthor(),
                    b.getCategory(),
                    b.getStatus()
            });
        }
    }

    @Override
    public void loadBooks() {
        try {
            List<LibraryBook> books = fetchBooks("");
            fillTable(books);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Could not load books:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void addBook() {
        String title = txtTitle.getText().trim();
        String author = txtAuthor.getText().trim();
        String category = txtCategory.getText().trim();
        String status = txtStatus.getText().trim();

        if (title.isEmpty() || author.isEmpty() || category.isEmpty() || status.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill title, author, category, and status.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "INSERT INTO tblbooks (title, author, category, status) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, title);
            ps.setString(2, author);
            ps.setString(3, category);
            ps.setString(4, status);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Book added.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            applySearchFilter();
            clearFields();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void updateBook() {
        if (selectedBookId < 0) {
            JOptionPane.showMessageDialog(this, "Select a book from the table first.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int bookId = selectedBookId;

        String title = txtTitle.getText().trim();
        String author = txtAuthor.getText().trim();
        String category = txtCategory.getText().trim();
        String status = txtStatus.getText().trim();

        if (title.isEmpty() || author.isEmpty() || category.isEmpty() || status.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill title, author, category, and status.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "UPDATE tblbooks SET title = ?, author = ?, category = ?, status = ? WHERE bookid = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, title);
            ps.setString(2, author);
            ps.setString(3, category);
            ps.setString(4, status);
            ps.setInt(5, bookId);

            int rows = ps.executeUpdate();
            if (rows == 0) {
                JOptionPane.showMessageDialog(this, "No book found with that ID.",
                        "Update", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JOptionPane.showMessageDialog(this, "Book updated.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            applySearchFilter();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void deleteBook() {
        if (selectedBookId < 0) {
            JOptionPane.showMessageDialog(this, "Select a book from the table first.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int bookId = selectedBookId;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete book ID " + bookId + "?",
                "Confirm delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = "DELETE FROM tblbooks WHERE bookid = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bookId);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                JOptionPane.showMessageDialog(this, "No book found with that ID.",
                        "Delete", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JOptionPane.showMessageDialog(this, "Book deleted.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            applySearchFilter();
            clearFields();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void clearFields() {
        selectedBookId = -1;
        txtTitle.setText("");
        txtAuthor.setText("");
        txtCategory.setText("");
        txtStatus.setText("");
        tblBooks.clearSelection();
    }
}
