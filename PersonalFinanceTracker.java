import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;

public class PersonalFinanceTracker extends JFrame {

    // Models for data storage
    private ArrayList<Transaction> transactions;
    private DefaultTableModel transactionTableModel;

    // UI Components
    private JTable transactionTable;
    private JTextField dateField, amountField, descriptionField;
    private JComboBox<String> categoryComboBox;
    private JComboBox<String> typeComboBox;
    private JLabel balanceLabel;
    private double currentBalance = 0.0;

    // Date formatter
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    // Constructor
    public PersonalFinanceTracker() {
        transactions = new ArrayList<>();
        initUI();
        loadData();
        updateBalance();
    }

    // Initialize the UI components
    private void initUI() {
        setTitle("Personal Finance Tracker");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create panels
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Form panel for adding transactions
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 5, 5));

        // Date field
        formPanel.add(new JLabel("Date (yyyy-MM-dd):"));
        dateField = new JTextField(dateFormat.format(new Date()));
        formPanel.add(dateField);

        // Amount field
        formPanel.add(new JLabel("Amount:"));
        amountField = new JTextField();
        formPanel.add(amountField);

        // Transaction type
        formPanel.add(new JLabel("Type:"));
        String[] types = {"Expense", "Income", "Transfer"};
        typeComboBox = new JComboBox<>(types);
        formPanel.add(typeComboBox);

        // Categories
        formPanel.add(new JLabel("Category:"));
        String[] categories = {"Food", "Transportation", "Housing", "Entertainment", "Utilities", "Health", "Education", "Salary", "Investment", "Other"};
        categoryComboBox = new JComboBox<>(categories);
        formPanel.add(categoryComboBox);

        // Description field
        formPanel.add(new JLabel("Description:"));
        descriptionField = new JTextField();
        formPanel.add(descriptionField);

        // Add transaction button
        JButton addButton = new JButton("Add Transaction");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addTransaction();
            }
        });
        formPanel.add(addButton);

        // Cancel/Clear button
        JButton clearButton = new JButton("Clear Form");
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearForm();
            }
        });
        formPanel.add(clearButton);

        // Transaction table
        String[] columnNames = {"Date", "Type", "Category", "Amount", "Description"};
        transactionTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        transactionTable = new JTable(transactionTableModel);
        JScrollPane scrollPane = new JScrollPane(transactionTable);

        // Summary panel
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        balanceLabel = new JLabel("Current Balance: $0.00");
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 16));
        summaryPanel.add(balanceLabel);

        // Button panel for actions
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton deleteButton = new JButton("Delete Selected");
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelectedTransaction();
            }
        });
        buttonPanel.add(deleteButton);

        JButton exportButton = new JButton("Export Data");
        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportData();
            }
        });
        buttonPanel.add(exportButton);

        // Add components to the main panel
        mainPanel.add(formPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(summaryPanel, BorderLayout.SOUTH);
        mainPanel.add(buttonPanel, BorderLayout.EAST);

        // Set the main panel to the frame
        setContentPane(mainPanel);
    }

    // Add a new transaction
    private void addTransaction() {
        try {
            // Validate input
            if (dateField.getText().trim().isEmpty() || amountField.getText().trim().isEmpty()
                    || descriptionField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Parse date
            Date date = dateFormat.parse(dateField.getText());

            // Parse amount
            double amount = Double.parseDouble(amountField.getText());

            // Get type and category
            String type = (String) typeComboBox.getSelectedItem();
            String category = (String) categoryComboBox.getSelectedItem();

            // Get description
            String description = descriptionField.getText();

            // Create and add transaction
            Transaction transaction = new Transaction(date, type, category, amount, description);
            transactions.add(transaction);

            // Update table
            updateTransactionTable();

            // Update balance
            updateBalance();

            // Save data
            saveData();

            // Clear the form
            clearForm();

        } catch (ParseException e) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Please use yyyy-MM-dd", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid amount. Please enter a number", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Delete the selected transaction
    private void deleteSelectedTransaction() {
        int selectedRow = transactionTable.getSelectedRow();
        if (selectedRow >= 0) {
            transactions.remove(selectedRow);
            updateTransactionTable();
            updateBalance();
            saveData();
        } else {
            JOptionPane.showMessageDialog(this, "Please select a transaction to delete", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Clear the form fields
    private void clearForm() {
        dateField.setText(dateFormat.format(new Date()));
        amountField.setText("");
        typeComboBox.setSelectedIndex(0);
        categoryComboBox.setSelectedIndex(0);
        descriptionField.setText("");
    }

    // Update the transaction table with current data
    private void updateTransactionTable() {
        transactionTableModel.setRowCount(0);

        for (Transaction transaction : transactions) {
            Object[] rowData = {
                    dateFormat.format(transaction.getDate()),
                    transaction.getType(),
                    transaction.getCategory(),
                    String.format("$%.2f", transaction.getAmount()),
                    transaction.getDescription()
            };
            transactionTableModel.addRow(rowData);
        }
    }

    // Calculate and update the current balance
    private void updateBalance() {
        currentBalance = 0.0;

        for (Transaction transaction : transactions) {
            if (transaction.getType().equals("Income")) {
                currentBalance += transaction.getAmount();
            } else if (transaction.getType().equals("Expense")) {
                currentBalance -= transaction.getAmount();
            }
            // Transfers don't affect the balance
        }

        balanceLabel.setText("Current Balance: " + NumberFormat.getCurrencyInstance().format(currentBalance));
    }

    // Save data to file
    private void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("finance_data.dat"))) {
            oos.writeObject(transactions);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Load data from file
    @SuppressWarnings("unchecked")
    private void loadData() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("finance_data.dat"))) {
            transactions = (ArrayList<Transaction>) ois.readObject();
            updateTransactionTable();
        } catch (FileNotFoundException e) {
            // First run, no data file yet
        } catch (IOException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Export data to CSV
    private void exportData() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Export Data");

            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();

                // Ensure it has .csv extension
                String path = file.getAbsolutePath();
                if (!path.endsWith(".csv")) {
                    file = new File(path + ".csv");
                }

                try (PrintWriter writer = new PrintWriter(file)) {
                    // Write header
                    writer.println("Date,Type,Category,Amount,Description");

                    // Write data
                    for (Transaction transaction : transactions) {
                        writer.println(String.format("%s,%s,%s,%.2f,%s",
                                dateFormat.format(transaction.getDate()),
                                transaction.getType(),
                                transaction.getCategory(),
                                transaction.getAmount(),
                                transaction.getDescription().replace(",", " ")));
                    }

                    JOptionPane.showMessageDialog(this, "Data exported successfully to " + file.getName());
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error exporting data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Main method
    public static void main(String[] args) {
        // Set look and feel to system
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Launch the application
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new PersonalFinanceTracker().setVisible(true);
            }
        });
    }

    // Transaction class
    private static class Transaction implements Serializable {
        private static final long serialVersionUID = 1L;

        private Date date;
        private String type;
        private String category;
        private double amount;
        private String description;

        public Transaction(Date date, String type, String category, double amount, String description) {
            this.date = date;
            this.type = type;
            this.category = category;
            this.amount = amount;
            this.description = description;
        }

        public Date getDate() {
            return date;
        }

        public String getType() {
            return type;
        }

        public String getCategory() {
            return category;
        }

        public double getAmount() {
            return amount;
        }

        public String getDescription() {
            return description;
        }
    }
}
