import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeManagementApp extends JFrame {
    private JTextField nameField, departmentField, birthdateField, phoneField, emailField, contractField, addressField;
    private JPasswordField passwordField;
    private JTable employeeTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> employeeComboBox;
    private final List<Employee> employees;
    private final File csvFile;

    public EmployeeManagementApp() {
        employees = new ArrayList<>();
        csvFile = new File("employees.csv");
        setTitle("Employee Management Application");
        setSize(1050, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // UI stuff
        initUI();

        loadEmployeesFromCSV();
        setVisible(true);
    }

    // UI stuff
    private void initUI() {
        JTabbedPane tabbedPane = new JTabbedPane();
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new GridLayout(15, 2, 5, 5));

        // Initialize text fields
        nameField = new JTextField();
        departmentField = new JTextField();
        birthdateField = new JTextField();
        phoneField = new JTextField();
        emailField = new JTextField();
        contractField = new JTextField();
        addressField = new JTextField();
        passwordField = new JPasswordField();

        // the thing that makes the phoneField stop taking in letters and capping it at 11 characters
        phoneField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent evt) {
                char c = evt.getKeyChar();
                if (!(Character.isDigit(c) || c == KeyEvent.VK_BACK_SPACE || c == KeyEvent.VK_DELETE) || phoneField.getText().length() >= 11) {
                    evt.consume();
                }
            }
        });

        // information Pane's things
        infoPanel.add(new JLabel("Name:"));
        infoPanel.add(nameField);
        infoPanel.add(new JLabel("Department:"));
        infoPanel.add(departmentField);
        infoPanel.add(new JLabel("Birthdate:"));
        infoPanel.add(birthdateField);
        infoPanel.add(new JLabel("Phone:"));
        infoPanel.add(phoneField);
        infoPanel.add(new JLabel("Email:"));
        infoPanel.add(emailField);
        infoPanel.add(new JLabel("Contract:"));
        infoPanel.add(contractField);
        infoPanel.add(new JLabel("Address:"));
        infoPanel.add(addressField);
        infoPanel.add(new JLabel("Password:"));
        infoPanel.add(passwordField);

        // Checkbox to show/hide password
        JCheckBox showPasswordCheckBox = new JCheckBox("Show Password");
        showPasswordCheckBox.addActionListener(e -> {
            if (showPasswordCheckBox.isSelected()) {
                passwordField.setEchoChar((char) 0);
            } else {
                passwordField.setEchoChar('*');
            }
        });
        infoPanel.add(new JLabel());
        infoPanel.add(showPasswordCheckBox);

        // Initialize combo box and buttons
        employeeComboBox = new JComboBox<>();
        employeeComboBox.addActionListener(e -> {
            String selectedItem = (String) employeeComboBox.getSelectedItem();
            if (selectedItem != null) {
                String[] parts = selectedItem.split(": ");
                int employeeId = Integer.parseInt(parts[0]);
                Employee employee = employees.stream().filter(emp -> emp.getId() == employeeId).findFirst().orElse(null);
                if (employee != null) {
                    populateFormFields(employee);
                }
            }
        });
        
        JButton logoutButton = new JButton("logout");
        JButton createButton = new JButton("Create");
        JButton updateButton = new JButton("Update");
        JButton deleteButton = new JButton("Delete");
        JButton editButton = new JButton("Edit");

        // Add action listeners to buttons
        createButton.addActionListener(e -> createEmployee());
        updateButton.addActionListener(e -> {
            editEmployee();
            saveEmployeesToCSV(); // Save changes to CSV after editing
            updateEmployeeList(); // Refresh UI
        });
        deleteButton.addActionListener(e -> deleteEmployee());
        editButton.addActionListener(e -> {
            String selectedItem = (String) employeeComboBox.getSelectedItem();
            if (selectedItem != null) {
                String[] parts = selectedItem.split(": ");
                int employeeId = Integer.parseInt(parts[0]);
                Employee employee = employees.stream().filter(emp -> emp.getId() == employeeId).findFirst().orElse(null);
                if (employee != null) {
                    populateFormFields(employee);
                }
            }
        });
        
    logoutButton.addActionListener(e -> {
     System.out.println("Logging out");
        // Open the login system window
        new Loginsystem().setVisible(true);
         // Close the current window
         this.dispose();
        });

            
            
        // Add buttons and combo box to button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.add(logoutButton); //new function
        buttonPanel.add(createButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(editButton);
        buttonPanel.add(new JLabel("Read:"));
        buttonPanel.add(employeeComboBox);

        // Combine panels
        JPanel combinedPanel = new JPanel();
        combinedPanel.setLayout(new BorderLayout());
        combinedPanel.add(infoPanel, BorderLayout.CENTER);
        combinedPanel.add(buttonPanel, BorderLayout.SOUTH);

        tabbedPane.add("Information", combinedPanel);

        // Payroll panel THIS IS THE PAYROLL PANEL'S LOOK, NOT THE FUNCTIONS
        JPanel payrollPanel = new JPanel();
        payrollPanel.setLayout(new GridLayout(6, 2, 5, 5));
        payrollPanel.add(new JLabel("Basic Salary:"));
        JTextField basicSalaryField = new JTextField();
        payrollPanel.add(basicSalaryField);
        payrollPanel.add(new JLabel("Raise Subsidy:"));
        JTextField raisePositionField = new JTextField();
        payrollPanel.add(raisePositionField);
        payrollPanel.add(new JLabel("Position Raise:"));
        JTextField paymentBonusField = new JTextField();
        payrollPanel.add(paymentBonusField);
        payrollPanel.add(new JLabel("Inflation Counter:"));
        JTextField inflationField = new JTextField();
        payrollPanel.add(inflationField);
        JButton computeButton = new JButton("Compute");
        computeButton.addActionListener(e -> computePayroll(basicSalaryField, raisePositionField, paymentBonusField, inflationField));
        payrollPanel.add(computeButton);
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> clearPayrollFields(basicSalaryField, raisePositionField, paymentBonusField, inflationField));
        payrollPanel.add(clearButton);
        tabbedPane.add("Payroll", payrollPanel);

        // Table's formation
        tableModel = new DefaultTableModel(new String[]{"ID", "Name", "Department", "Birthdate", "Phone", "Email", "Contract", "Address"}, 0);
        employeeTable = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(employeeTable);
        add(tabbedPane, BorderLayout.CENTER);
        add(tableScrollPane, BorderLayout.EAST);
    }

    // Populate form fields with employee details
    private void populateFormFields(Employee employee) {
        nameField.setText(employee.getName());
        departmentField.setText(employee.getDepartment());
        birthdateField.setText(employee.getBirthdate());
        phoneField.setText(employee.getPhone());
        emailField.setText(employee.getEmail());
        contractField.setText(employee.getContract());
        addressField.setText(employee.getAddress());
        passwordField.setText(employee.getPassword());
    }

    // Create a new employee
    private void createEmployee() {
        String name = nameField.getText();
        String department = departmentField.getText();
        String birthdate = birthdateField.getText();
        String phone = phoneField.getText();
        String email = emailField.getText();
        String contract = contractField.getText();
        String address = addressField.getText();
        String password = new String(passwordField.getPassword());

        // Validate fields
        if (name.isEmpty() || department.isEmpty() || birthdate.isEmpty() || phone.isEmpty() || email.isEmpty() || contract.isEmpty() || address.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Error: All fields must be filled", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create employee object and add to list
        Employee employee = new Employee(name, department, birthdate, phone, email, contract, address, password);
        employees.add(employee);

        // Save to CSV and update UI
        saveEmployeesToCSV();
        updateEmployeeList();
        clearInputFields();
    }

    // Update employee list in combo box and table
    private void updateEmployeeList() {
        employeeComboBox.removeAllItems();
        tableModel.setRowCount(0);
        for (Employee employee : employees) {
            employeeComboBox.addItem(employee.getId() + ": " + employee.getName());
            tableModel.addRow(new Object[]{employee.getId(), employee.getName(), employee.getDepartment(), employee.getBirthdate(), employee.getPhone(), employee.getEmail(), employee.getContract(), employee.getAddress()});
        }
    }

    // Delete selected employee
    private void deleteEmployee() {
        String selectedItem = (String) employeeComboBox.getSelectedItem();
        if (selectedItem != null) {
            String[] parts = selectedItem.split(": ");
            int employeeId = Integer.parseInt(parts[0]);
            employees.removeIf(employee -> employee.getId() == employeeId);
            saveEmployeesToCSV();
            updateEmployeeList();
        }
    }

    // Edit selected employee
    private void editEmployee() {
        String selectedItem = (String) employeeComboBox.getSelectedItem();
        if (selectedItem != null) {
            String[] parts = selectedItem.split(": ");
            int employeeId = Integer.parseInt(parts[0]);
            Employee employee = employees.stream().filter(emp -> emp.getId() == employeeId).findFirst().orElse(null);
            if (employee != null) {
                employee.setName(nameField.getText());
                employee.setDepartment(departmentField.getText());
                employee.setBirthdate(birthdateField.getText());
                employee.setPhone(phoneField.getText());
                employee.setEmail(emailField.getText());
                employee.setContract(contractField.getText());
                employee.setAddress(addressField.getText());
                employee.setPassword(new String(passwordField.getPassword()));
            }
        }
    }

    // Load employees from CSV file
    private void loadEmployeesFromCSV() {
        if (!csvFile.exists()) {
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println("Reading line: " + line);  // Debugging output
                String[] values = parseCSVLine(line);
                System.out.println("Number of fields: " + values.length);  // Debugging output

                if (values.length == 9) {
                    Employee employee = new Employee(values[1], values[2], values[3], values[4], values[5], values[6], values[7], values[8]);
                    employee.setId(Integer.parseInt(values[0]));
                    employees.add(employee);
                } else {
                    System.err.println("Error: Invalid line format in CSV file: " + line);
                    System.err.println("Found " + values.length + " fields, expected 9.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        updateEmployeeList();
    }

    // Save employees to CSV file
    private void saveEmployeesToCSV() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(csvFile))) {
            int id = 1;
            for (Employee employee : employees) {
                employee.setId(id++);
                String csvLine = employee.toCSVString();
                System.out.println("Writing line: " + csvLine);  // Debugging output
                bw.write(csvLine);
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Clear input fields
    private void clearInputFields() {
        nameField.setText("");
        departmentField.setText("");
        birthdateField.setText("");
        phoneField.setText("");
        emailField.setText("");
        contractField.setText("");
        addressField.setText("");
        passwordField.setText("");
    }

    // payroll math
    private void computePayroll(JTextField basicSalaryField, JTextField raisePositionField, JTextField bonusPaymentField, JTextField inflationField) {
    try {
        double basicSalary = Double.parseDouble(basicSalaryField.getText());
        double raisePosition = Double.parseDouble(raisePositionField.getText());
        double bonusField = Double.parseDouble(bonusPaymentField.getText());
        double inflationCounter = Double.parseDouble(inflationField.getText());

        // Example deduction rates and computations
        double sss = basicSalary * 0.045;
        double philHealth = basicSalary * 0.02;
        double pagIbig = basicSalary * 0.01;
        double tax = basicSalary * 0.1;

        // Total deductions
        double totalDeductions = sss + philHealth + pagIbig + tax;

        // Total allowances
        double totalAllowances = raisePosition + bonusField + inflationCounter;

        // Net salary computation
        double netSalary = basicSalary + totalAllowances - totalDeductions;

        // Display computed payroll
        JOptionPane.showMessageDialog(this,
            "Payroll Computation:\n" +
            "Basic Salary: " + basicSalary + "\n" +
            "Raise Position: " + raisePosition + "\n" +
            "Bonus: " + bonusField + "\n" +
            "Inflation Payment: " + inflationCounter + "\n" +
            "SSS: " + sss + "\n" +
            "PhilHealth: " + philHealth + "\n" +
            "Pag-IBIG: " + pagIbig + "\n" +
            "Tax: " + tax + "\n" +
            "Total Deductions: " + totalDeductions + "\n" +
            "Total Allowances: " + totalAllowances + "\n" +
            "Net Salary: " + netSalary,
            "Payroll Computation Result",
            JOptionPane.INFORMATION_MESSAGE
        );
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, "Please enter valid numbers for payroll computation.", "Input Error", JOptionPane.ERROR_MESSAGE);
    }
}

    private void clearPayrollFields(JTextField basicSalaryField, JTextField raisePositionField, JTextField bonusPaymentField, JTextField inflationField) {
        //AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
        basicSalaryField.setText("");
        inflationField.setText("");
        raisePositionField.setText("");
        bonusPaymentField.setText("");
    }

    // Employee class
    static class Employee {
        private static int nextId = 1;
        private int id;
        private String name, department, birthdate, phone, email, contract, address, password;

        public Employee(String name, String department, String birthdate, String phone, String email, String contract, String address, String password) {
            this.name = name;
            this.department = department;
            this.birthdate = birthdate;
            this.phone = phone;
            this.email = email;
            this.contract = contract;
            this.address = address;
            this.password = password;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDepartment() {
            return department;
        }

        public void setDepartment(String department) {
            this.department = department;
        }

        public String getBirthdate() {
            return birthdate;
        }

        public void setBirthdate(String birthdate) {
            this.birthdate = birthdate;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getContract() {
            return contract;
        }

        public void setContract(String contract) {
            this.contract = contract;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String toCSVString() {
            return id + "," + name + "," + department + "," + birthdate + "," + phone + "," + email + "," + contract + "," + address + "," + password;
        }
    }

    // Main method to launch the application
    public static void main(String[] args) {
        SwingUtilities.invokeLater(EmployeeManagementApp::new);
    }

    // Parse CSV line into array of values
    private String[] parseCSVLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;
        for (char c : line.toCharArray()) {
            if (c == ',' && !inQuotes) {
                values.add(currentField.toString());
                currentField.setLength(0);
            } else if (c == '"') {
                inQuotes = !inQuotes;
            } else {
                currentField.append(c);
            }
        }
        values.add(currentField.toString());
        return values.toArray(new String[0]);
    }
}
//leaving this comment to update the commit
// 10/02/2025, what.