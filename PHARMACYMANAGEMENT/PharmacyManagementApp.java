
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;

// Pharmacy Operations Interface
@SuppressWarnings("unused")
interface PharmacyOperations {
    void addMedicine(Medicine medicine) throws SQLException;
    void removeMedicine(String name) throws InvalidMedicineException, SQLException;
    void reduceQuantity(String name, int amount) throws InvalidMedicineException, SQLException;
    boolean processCustomerOrder(String name, int quantity) throws InvalidMedicineException, SQLException;
    ArrayList<Medicine> loadMedicines() throws SQLException;
}

// Custom Exception for Invalid Medicines
class InvalidMedicineException extends Exception {
    public InvalidMedicineException(String message) {
        super(message);
    }
}

// Medicine Class
class Medicine {
    String name;
    double price;
    int quantity;

    public Medicine(String name, double price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public Object[] getRowData() {
        return new Object[]{name, price, quantity};
    }
}

// Pharmacy Class with MySQL integration
class Pharmacy implements PharmacyOperations {
    private Connection conn;

    public Pharmacy() throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver"); // Load the driver
        // Establish database connection
        conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/pharmacy_db", "root", "Akhil1234!");

        // Create medicines table
        String createTableQuery = """
            CREATE TABLE IF NOT EXISTS medicines (
                name VARCHAR(100) PRIMARY KEY,
                price DOUBLE NOT NULL,
                quantity INT NOT NULL
            )
        """;
        conn.createStatement().execute(createTableQuery);
    }

    @Override
    public void addMedicine(Medicine medicine) throws SQLException {
        String query = "INSERT INTO medicines (name, price, quantity) VALUES (?, ?, ?) " +
                       "ON DUPLICATE KEY UPDATE price = ?, quantity = quantity + ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, medicine.name);
        stmt.setDouble(2, medicine.price);
        stmt.setInt(3, medicine.quantity);
        stmt.setDouble(4, medicine.price);
        stmt.setInt(5, medicine.quantity);
        stmt.executeUpdate();
    }

    @Override
    public void removeMedicine(String name) throws InvalidMedicineException, SQLException {
        String query = "DELETE FROM medicines WHERE name = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, name);
        int rowsAffected = stmt.executeUpdate();
        if (rowsAffected == 0) {
            throw new InvalidMedicineException("Medicine not found: " + name);
        }
    }

    @Override
    public void reduceQuantity(String name, int amount) throws InvalidMedicineException, SQLException {
        String selectQuery = "SELECT quantity FROM medicines WHERE name = ?";
        PreparedStatement selectStmt = conn.prepareStatement(selectQuery);
        selectStmt.setString(1, name);
        ResultSet rs = selectStmt.executeQuery();

        if (rs.next()) {
            int currentQuantity = rs.getInt("quantity");
            if (currentQuantity < amount) {
                throw new InvalidMedicineException("Not enough stock to remove " + amount + " units.");
            }
            int newQuantity = currentQuantity - amount;

            String updateQuery = newQuantity == 0
                    ? "DELETE FROM medicines WHERE name = ?"
                    : "UPDATE medicines SET quantity = ? WHERE name = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
            if (newQuantity == 0) {
                updateStmt.setString(1, name);
            } else {
                updateStmt.setInt(1, newQuantity);
                updateStmt.setString(2, name);
            }
            updateStmt.executeUpdate();
        } else {
            throw new InvalidMedicineException("Medicine not found: " + name);
        }
    }
    public void finalizeCustomerOrder(String medicineName, int quantity) {
        // Log or process the order as finalized
        System.out.println("Order finalized for: " + medicineName + " Quantity: " + quantity);
        // Any additional operations can be added here
    }
    

    @Override
    public boolean processCustomerOrder(String name, int quantity) throws InvalidMedicineException, SQLException {
        reduceQuantity(name, quantity);
        return true;
    }
    public void restoreCart(HashMap<String, Integer> cart) throws SQLException {
        for (Map.Entry<String, Integer> entry : cart.entrySet()) {
            String name = entry.getKey();
            int quantity = entry.getValue();
    
            String query = "INSERT INTO medicines (name, price, quantity) VALUES (?, ?, ?) " +
                           "ON DUPLICATE KEY UPDATE quantity = quantity + ?";
            PreparedStatement stmt = conn.prepareStatement(query);
    
            // Fetch price for the medicine
            String selectQuery = "SELECT price FROM medicines WHERE name = ?";
            PreparedStatement selectStmt = conn.prepareStatement(selectQuery);
            selectStmt.setString(1, name);
            ResultSet rs = selectStmt.executeQuery();
    
            double price = 0;
            if (rs.next()) {
                price = rs.getDouble("price");
            } else {
                // Default price handling if medicine is removed completely
                price = 0; // or some default value
            }
    
            stmt.setString(1, name);
            stmt.setDouble(2, price);
            stmt.setInt(3, quantity);
            stmt.setInt(4, quantity);
            stmt.executeUpdate();
        }
    }
    

    @Override
    public ArrayList<Medicine> loadMedicines() throws SQLException {
        ArrayList<Medicine> medicines = new ArrayList<>();
        String query = "SELECT * FROM medicines";
        ResultSet rs = conn.createStatement().executeQuery(query);
        while (rs.next()) {
            medicines.add(new Medicine(rs.getString("name"), rs.getDouble("price"), rs.getInt("quantity")));
        }
        return medicines;
    }
}

// Main Application
public class PharmacyManagementApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                Pharmacy pharmacy = new Pharmacy();
                HashMap<String, Integer> cart = new HashMap<>();

                JFrame frame = new JFrame("Pharmacy Management System");
                frame.setSize(600, 400);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                CardLayout cardLayout = new CardLayout();
                JPanel cardPanel = new JPanel(cardLayout);

                // Login Panel
                JPanel loginPanel = new JPanel();
                loginPanel.setLayout(new GridLayout(3, 1));
                JLabel loginLabel = new JLabel("Select Role", SwingConstants.CENTER);
                JButton customerButton = new JButton("Customer");
                JButton managerButton = new JButton("Manager");

                loginPanel.add(loginLabel);
                loginPanel.add(customerButton);
                loginPanel.add(managerButton);
                cardPanel.add(loginPanel, "Login");

                // Customer Panel
                JPanel customerPanel = new JPanel(new BorderLayout());
                DefaultTableModel medicineModel = new DefaultTableModel(new String[]{"Name", "Price", "Quantity"}, 0);
                JTable medicineTable = new JTable(medicineModel);
                JScrollPane scrollPane = new JScrollPane(medicineTable);
                customerPanel.add(scrollPane, BorderLayout.CENTER);

                JPanel actionPanel = new JPanel();
                JTextField quantityField = new JTextField(10);
                JButton addToCartButton = new JButton("Add to Cart");
                JButton viewCartButton = new JButton("View Cart");
                JButton paymentButton = new JButton("Payment");
                JButton refreshButton = new JButton("Refresh");
                actionPanel.add(refreshButton);

                
                actionPanel.add(new JLabel("Quantity:"));
                actionPanel.add(quantityField);
                actionPanel.add(addToCartButton);
                actionPanel.add(viewCartButton);
                actionPanel.add(paymentButton);

                customerPanel.add(actionPanel, BorderLayout.SOUTH);



                ArrayList<Medicine> medicines = pharmacy.loadMedicines();
                for (Medicine m : medicines) {
                    medicineModel.addRow(m.getRowData());
                }


                addToCartButton.addActionListener(e -> {
                    try {
                        int selectedRow = medicineTable.getSelectedRow();
                        if (selectedRow == -1) {
                            JOptionPane.showMessageDialog(frame, "Select a medicine to add to cart.");
                            return;
                        }
                
                        String name = (String) medicineTable.getValueAt(selectedRow, 0);
                        double price = (Double) medicineTable.getValueAt(selectedRow, 1);
                        int availableQuantity = (Integer) medicineTable.getValueAt(selectedRow, 2);
                        int quantityToAdd = Integer.parseInt(quantityField.getText());
                
                        if (quantityToAdd <= 0 || quantityToAdd > availableQuantity) {
                            JOptionPane.showMessageDialog(frame, "Invalid quantity. Please check the available stock.");
                            return;
                        }
                
                        cart.put(name, cart.getOrDefault(name, 0) + quantityToAdd); // Add to cart
                        pharmacy.reduceQuantity(name, quantityToAdd); // Update database
                
                        // Update table
                        medicineTable.setValueAt(availableQuantity - quantityToAdd, selectedRow, 2);
                
                        JOptionPane.showMessageDialog(frame, quantityToAdd + " units of " + name + " added to cart.");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
                    }
                });
                // Load Medicines and Update Table Function
                Runnable loadMedicinesIntoTable = () -> {
                    try {
                        medicineModel.setRowCount(0); // Clear existing rows
                        ArrayList<Medicine> updatedMedicines = pharmacy.loadMedicines();
                        for (Medicine m : updatedMedicines) {
                            medicineModel.addRow(m.getRowData());
                        }
                        JOptionPane.showMessageDialog(frame, "Medicine data refreshed successfully.");
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(frame, "Error refreshing data: " + ex.getMessage());
                    }
                };
                refreshButton.addActionListener(e -> {
                    loadMedicinesIntoTable.run();
                });
                

                viewCartButton.addActionListener(e -> {
                    DefaultTableModel cartModel = new DefaultTableModel(new String[]{"Name", "Quantity"}, 0);
                    for (Map.Entry<String, Integer> entry : cart.entrySet()) {
                        cartModel.addRow(new Object[]{entry.getKey(), entry.getValue()});
                    }
                    JTable cartTable = new JTable(cartModel);
                    JOptionPane.showMessageDialog(frame, new JScrollPane(cartTable), "Your Cart", JOptionPane.PLAIN_MESSAGE);
                });

                paymentButton.addActionListener(e -> {
                    StringBuilder receipt = new StringBuilder("Receipt:\n");
                    double total = 0;
                
                    for (Map.Entry<String, Integer> entry : cart.entrySet()) {
                        String name = entry.getKey();
                        int quantity = entry.getValue();
                        double price = medicines.stream()
                                .filter(m -> m.name.equals(name))
                                .findFirst()
                                .get()
                                .price;
                
                        total += quantity * price;
                        receipt.append(name)
                                .append(" x")
                                .append(quantity)
                                .append(" @ ")
                                .append(price)
                                .append(" = ")
                                .append(quantity * price)
                                .append("\n");
                    }
                    receipt.append("Total: ").append(total).append("\n");
                
                    int confirm = JOptionPane.showConfirmDialog(frame, receipt.toString() + "\nProceed with payment?");
                    if (confirm == JOptionPane.YES_OPTION) {
                        // Payment process
                        JPanel paymentPanel = new JPanel(new GridLayout(3, 2));
                        JTextField cardNumberField = new JTextField(16);
                        JTextField expiryField = new JTextField(5);
                        JTextField cvvField = new JTextField(3);
                
                        paymentPanel.add(new JLabel("Card Number:"));
                        paymentPanel.add(cardNumberField);
                        paymentPanel.add(new JLabel("Expiry (MM/YY):"));
                        paymentPanel.add(expiryField);
                        paymentPanel.add(new JLabel("CVV:"));
                        paymentPanel.add(cvvField);
                
                        int paymentConfirm = JOptionPane.showConfirmDialog(frame, paymentPanel, "Enter Payment Details", JOptionPane.OK_CANCEL_OPTION);
                
                        if (paymentConfirm == JOptionPane.OK_OPTION) {
                            try {
                                // Validate card number
                                String cardNumber = cardNumberField.getText().trim();
                                if (!cardNumber.matches("\\d{16}")) {
                                    JOptionPane.showMessageDialog(frame, "Invalid Card Number! It should be exactly 16 digits.");
                                    return;
                                }
                
                                // Validate expiry date
                                String expiry = expiryField.getText().trim();
                                if (!expiry.matches("(0[1-9]|1[0-2])/\\d{2}")) {
                                    JOptionPane.showMessageDialog(frame, "Invalid Expiry Date! Format should be MM/YY (e.g., 09/25).");
                                    return;
                                }
                
                                // Validate CVV
                                String cvv = cvvField.getText().trim();
                                if (!cvv.matches("\\d{3}")) {
                                    JOptionPane.showMessageDialog(frame, "Invalid CVV! It should be exactly 3 digits.");
                                    return;
                                }
                
                                // If all validations pass
                                cart.clear();
                                JOptionPane.showMessageDialog(frame, "Payment successful! Thank you for your purchase.");
                            } catch (Exception ex) {
                                JOptionPane.showMessageDialog(frame, "Error finalizing order: " + ex.getMessage());
                            }
                        }
                    } else {
                        try {
                            pharmacy.restoreCart(cart); // Restore items to database
                            cart.clear(); // Clear cart after restoration
                            JOptionPane.showMessageDialog(frame, "Payment cancelled. Items restored to inventory.");
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(frame, "Error restoring cart: " + ex.getMessage());
                        }
                    }
                });
                
                
                

                // Manager Panel
                JPanel managerPanel = new JPanel(new BorderLayout());
                JTextField nameField = new JTextField(10);
                JTextField priceField = new JTextField(10);
                JTextField stockField = new JTextField(10);
                JButton addButton = new JButton("Add Medicine");
                JButton removeButton = new JButton("Remove Medicine");
                JButton editQuantityButton = new JButton("Edit Quantity"); // New button for editing quantity
                JTable medicineTableForManager = new JTable(medicineModel); // Separate table for manager panel


                // Manager Panel: Add Medicine Button Action Listener
           // Manager Panel: Add Medicine Button Action Listener
addButton.addActionListener(e -> {
    try {
        // Get input values from the user
        String name = nameField.getText().trim();
        double price = Double.parseDouble(priceField.getText().trim());
        int quantity = Integer.parseInt(stockField.getText().trim());

        if (name.isEmpty() || price <= 0 || quantity <= 0) {
            JOptionPane.showMessageDialog(frame, "Please enter valid medicine details.");
            return;
        }

        // Create a Medicine object and add it to the database
        Medicine newMedicine = new Medicine(name, price, quantity);
        pharmacy.addMedicine(newMedicine);

        // Reflect changes in the table
        boolean isUpdated = false;
        for (int i = 0; i < medicineModel.getRowCount(); i++) {
            if (medicineModel.getValueAt(i, 0).equals(name)) {
                int updatedQuantity = (int) medicineModel.getValueAt(i, 2) + quantity;
                medicineModel.setValueAt(price, i, 1); // Update price
                medicineModel.setValueAt(updatedQuantity, i, 2); // Update quantity
                isUpdated = true;
                break;
            }
        }
        if (!isUpdated) {
            // Add a new row if medicine doesn't exist
            medicineModel.addRow(newMedicine.getRowData());
        }

        // Clear input fields after successful addition
        nameField.setText("");
        priceField.setText("");
        stockField.setText("");

        JOptionPane.showMessageDialog(frame, "Medicine added successfully!");

    } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(frame, "Please enter valid numeric values for price and quantity.");
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
        ex.printStackTrace();
    }
});
removeButton.addActionListener(e -> {
    try {
        int selectedRow = medicineTableForManager.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "Select a medicine to remove.");
            return;
        }

        String name = (String) medicineTableForManager.getValueAt(selectedRow, 0);
        pharmacy.removeMedicine(name); // Call removeMedicine method
        medicineModel.removeRow(selectedRow); // Remove row from table model

        JOptionPane.showMessageDialog(frame, "Medicine removed successfully!");
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
    }
});
editQuantityButton.addActionListener(e -> {
    try {
        int selectedRow = medicineTableForManager.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "Select a medicine to edit quantity.");
            return;
        }

        String name = (String) medicineTableForManager.getValueAt(selectedRow, 0);
        int currentQuantity = (int) medicineTableForManager.getValueAt(selectedRow, 2);
        double originalPrice = (double) medicineTableForManager.getValueAt(selectedRow, 1); // Retrieve original price

        // Prompt user for new quantity
        JPanel editQuantityPanel = new JPanel(new GridLayout(2, 2));
        editQuantityPanel.add(new JLabel("New Quantity:"));
        JTextField newQuantityField = new JTextField(10);
        editQuantityPanel.add(newQuantityField);

        int result = JOptionPane.showConfirmDialog(frame, editQuantityPanel, "Edit Quantity", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            int newQuantity = Integer.parseInt(newQuantityField.getText());
            if (newQuantity < 0) {
                JOptionPane.showMessageDialog(frame, "Invalid quantity. Please enter a non-negative value.");
                return;
            }
            pharmacy.reduceQuantity(name, currentQuantity); // Reduce current quantity to 0
            pharmacy.addMedicine(new Medicine(name, originalPrice, newQuantity)); // Add new quantity with original price
            medicineTableForManager.setValueAt(newQuantity, selectedRow, 2); // Update table

            JOptionPane.showMessageDialog(frame, "Quantity updated successfully!");
        }
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
    }
});


                JPanel managerActions = new JPanel();
                managerActions.add(new JLabel("Name:"));
                managerActions.add(nameField);
                managerActions.add(new JLabel("Price:"));
                managerActions.add(priceField);
                managerActions.add(new JLabel("Quantity:"));
                managerActions.add(stockField);
                managerActions.add(addButton);
                managerActions.add(removeButton);
                managerActions.add(editQuantityButton);

                managerPanel.add(managerActions, BorderLayout.NORTH);
                managerPanel.add(new JScrollPane(new JTable(medicineModel)), BorderLayout.CENTER);
                managerPanel.add(new JScrollPane(medicineTableForManager), BorderLayout.CENTER);

                cardPanel.add(customerPanel, "Customer");
                cardPanel.add(managerPanel, "Manager");

                customerButton.addActionListener(e -> {
                    JPanel customerLoginPanel = new JPanel(new GridLayout(2, 2)); // Renamed variable
                    customerLoginPanel.add(new JLabel("Username:"));
                    JTextField usernameField = new JTextField();
                    customerLoginPanel.add(usernameField);
                    customerLoginPanel.add(new JLabel("Password:"));
                    JPasswordField passwordField = new JPasswordField();
                    customerLoginPanel.add(passwordField);
                
                    int result = JOptionPane.showConfirmDialog(frame, customerLoginPanel, "Customer Login", JOptionPane.OK_CANCEL_OPTION);
                
                    if (result == JOptionPane.OK_OPTION) {
                        String username = usernameField.getText();
                        String password = new String(passwordField.getPassword());
                        if (username.equals("Customer") && password.equals("password")) {
                            cardLayout.show(cardPanel, "Customer");
                        } else {
                            JOptionPane.showMessageDialog(frame, "Invalid username or password for Customer.");
                        }
                    }
                });
                
                
                managerButton.addActionListener(e -> {
                    JPanel managerLoginPanel = new JPanel(new GridLayout(2, 2)); // Renamed variable
                    managerLoginPanel.add(new JLabel("Username:"));
                    JTextField usernameField = new JTextField();
                    managerLoginPanel.add(usernameField);
                    managerLoginPanel.add(new JLabel("Password:"));
                    JPasswordField passwordField = new JPasswordField();
                    managerLoginPanel.add(passwordField);
                
                    int result = JOptionPane.showConfirmDialog(frame, managerLoginPanel, "Manager Login", JOptionPane.OK_CANCEL_OPTION);
                
                    if (result == JOptionPane.OK_OPTION) {
                        String username = usernameField.getText();
                        String password = new String(passwordField.getPassword());
                        if (username.equals("Manager") && password.equals("password")) {
                            cardLayout.show(cardPanel, "Manager");
                        } else {
                            JOptionPane.showMessageDialog(frame, "Invalid username or password for Manager.");
                        }
                    }
                });


                frame.add(cardPanel);
                cardLayout.show(cardPanel, "Login");
                frame.setVisible(true);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
