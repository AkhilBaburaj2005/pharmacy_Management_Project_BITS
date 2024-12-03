 Pharmacy_Management_Project_BITS
Pharmacy Management System A Java Swing-based app with MySQL backend for managing pharmacy operations. Customers can browse and purchase medicines, while Managers handle inventory tasks like adding, updating, and deleting items. Demonstrates OOP principles, JDBC integration, and an intuitive GUI for seamless user experience.

# Pharmacy Management System

## Description
The Pharmacy Management System is a Java-based desktop application designed to streamline the operations of a pharmacy. This project employs a Graphical User Interface (GUI) built with Java Swing and integrates a MySQL database for efficient medicine inventory management. 

The application supports two user roles:
1. **Customers**: Browse medicines, add items to a cart, and proceed to payment with card validation.
2. **Managers**: Add, remove, and update stock details in the inventory through a dedicated management panel.

## Features
- **Customer Role**:
  - View available medicines in a table format.
  - Add medicines to the cart with quantity validation.
  - Review orders and generate a receipt.
  - Simulate payment with basic card validation.

- **Manager Role**:
  - Manage inventory (add, update, delete medicines).
  - Monitor stock levels and update prices.

- **Database Integration**:
  - Persistent storage of medicines and stock using MySQL.
  - Seamless integration via Java Database Connectivity (JDBC).

- **Robust User Interface**:
  - Built with Java Swing components like `JFrame`, `JPanel`, `JTable`, and `JOptionPane`.
  - Dynamic views powered by CardLayout for smooth navigation.

- **Data Structures**:
  - `HashMap` for managing cart items.
  - `ArrayList` for storing and displaying medicines.

## Tools Used
- **Java Swing**: GUI development.
- **MySQL**: Backend database for data persistence.
- **NetBeans IDE**: Development environment.
- **JDBC**: Database connectivity.

## Installation
1. Clone this repository to your local machine:
2. Set up the MySQL database:
- Create a database named `pharmacy_db`.
- Use the provided SQL script to create and populate the `medicines` table.
3. CREATE DATABASE pharmacy_db; USE pharmacy_db;

   CREATE TABLE IF NOT EXISTS medicines ( name VARCHAR(100) PRIMARY KEY, price DOUBLE NOT NULL, quantity INT NOT NULL );

  INSERT INTO medicines (name, price, quantity) VALUES ('Paracetamol', 1.50, 100), ('Ibuprofen', 2.00, 150), ('Aspirin', 0.75, 200), ('Amoxicillin', 10.00, 50), 
  ('Cough Syrup', 5.00, 80);
    4. Configure the database connection in the Java code:
    Update the database credentials in the `Pharmacy` class:
    ```java
  conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/pharmacy_db", "root", "your_password");

## Usage
Launch the application.
Log in as a Customer or Manager:
Default Customer credentials: Username: Customer, Password: password
Default Manager credentials: Username: Manager, Password: password

## Contributors
 - Adhitya Chandrajith (2023A7PS0022U)
 - Nirenjhena Venkatesan (2023A7PS0244U)
 - Akhil Baburaj (2023A7PS0013U)
 - Sreya Manoj (2023A7PS0396U)
 - Adyasha Mishra (2023A7PS0382U)
 - Yashvi Goradia (2023A7PS0087U)
## License
This project is licensed under the MIT License.

## Acknowledgments
 - Dr. Pranav Sir/Dr.Sapna Maam/Dr. Sujala maam/Dr. Neena for their valuable guidance.
 - Java Swing tutorials from W3Schools.
 - MySQL integration guidance from JavaTpoint.
