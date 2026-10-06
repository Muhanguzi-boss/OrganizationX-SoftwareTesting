import java.util.Scanner;
import java.util.function.DoubleFunction;
import java.util.function.UnaryOperator;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);

    private static final EmployeeManagementSystem system =
            new EmployeeManagementSystem();

    private static final int MAX_INPUT_LENGTH = 100;

    // Thrown when the user types "cancel" to go back to the menu
    private static class CancelledException extends RuntimeException {
    }

    public static void main(String[] args) {

        System.out.println("=================================");
        System.out.println(" ORGANIZATION X EMPLOYEE SYSTEM");
        System.out.println("=================================");

        boolean running = true;

        while (running) {

            printMenu();

            int choice = readMenuChoice(1, 8);

            try {
                switch (choice) {
                    case 1 -> addEmployee();
                    case 2 -> findEmployee();
                    case 3 -> updateEmployee();
                    case 4 -> removeEmployee();
                    case 5 -> calculateNetSalary();
                    case 6 -> System.out.println("\n" + system.generateEmployeeReport());
                    case 7 -> generatePayrollReport();
                    case 8 -> running = false;
                }
            } catch (CancelledException e) {
                System.out.println("  Cancelled. Nothing was changed.");
            } catch (IllegalArgumentException e) {
                // Safety net: the prompts should already have caught this
                System.out.println("  Sorry, that could not be completed: " + e.getMessage());
            }
        }

        System.out.println("\nThank you for using the Employee System. Goodbye!");
    }

    // --------------------------------------------------
    // Menu
    // --------------------------------------------------

    private static void printMenu() {
        System.out.println("\n----------- MAIN MENU -----------");
        System.out.println(" 1. Add employee");
        System.out.println(" 2. Find employee");
        System.out.println(" 3. Update employee");
        System.out.println(" 4. Remove employee");
        System.out.println(" 5. Calculate net salary");
        System.out.println(" 6. Employee report");
        System.out.println(" 7. Payroll report");
        System.out.println(" 8. Exit");
        System.out.println("---------------------------------");
    }

    private static int readMenuChoice(int min, int max) {

        while (true) {

            String input = readRawLine("Choose an option (" + min + "-" + max + "): ");

            if (input.isEmpty()) {
                System.out.println("  Oops! You didn't type anything. Please enter a number from the menu.");
            } else if (!input.matches("\\d+")) {
                System.out.println("  '" + input + "' is not a valid option. Please type a whole number like 1, 2 or 3.");
            } else if (input.length() > 2
                    || Integer.parseInt(input) < min
                    || Integer.parseInt(input) > max) {
                System.out.println("  '" + input + "' is not on the menu. Please pick a number between "
                        + min + " and " + max + ".");
            } else {
                return Integer.parseInt(input);
            }
        }
    }

    // --------------------------------------------------
    // Menu actions
    // --------------------------------------------------

    private static void addEmployee() {

        System.out.println("\n--- Add Employee ---");
        printCancelHint();

        String id = readValid("Employee ID (e.g. E001): ", input -> {
            String error = Validator.checkId(input);
            if (error == null && system.findEmployee(input) != null) {
                error = "An employee with ID '" + Validator.normalizeId(input)
                        + "' already exists. Please use a different ID.";
            }
            return error;
        });

        String name = readValid("Full name: ", Validator::checkName);
        String department = readDepartment(null);
        double salary = readSalary("Gross salary: ", null);

        system.addEmployee(new Employee(id, name, department, salary));

        System.out.println("  Employee " + Validator.normalizeId(id) + " ("
                + Validator.normalizeName(name) + ") was added successfully.");
    }

    private static void findEmployee() {

        System.out.println("\n--- Find Employee ---");

        if (!ensureEmployeesExist()) {
            return;
        }

        printCancelHint();

        Employee employee = readExistingEmployee();

        System.out.println("  Employee found:");
        System.out.println("    ID:         " + employee.getId());
        System.out.println("    Name:       " + employee.getName());
        System.out.println("    Department: " + employee.getDepartment());
        System.out.println("    Salary:     " + Validator.formatMoney(employee.getSalary()));
    }

    private static void updateEmployee() {

        System.out.println("\n--- Update Employee ---");

        if (!ensureEmployeesExist()) {
            return;
        }

        printCancelHint();

        Employee employee = readExistingEmployee();

        System.out.println("  Current details: " + employee);
        System.out.println("  (Press Enter to keep the current value.)");

        String name = readValid("New name [" + employee.getName() + "]: ",
                input -> input.isEmpty() ? null : Validator.checkName(input));

        if (name.isEmpty()) {
            name = employee.getName();
        }

        String department = readDepartment(employee.getDepartment());
        double salary = readSalary("New salary [" + Validator.formatMoney(employee.getSalary()) + "]: ",
                employee.getSalary());

        system.updateEmployee(employee.getId(), name, department, salary);

        System.out.println("  Employee " + employee.getId() + " was updated successfully.");
    }

    private static void removeEmployee() {

        System.out.println("\n--- Remove Employee ---");

        if (!ensureEmployeesExist()) {
            return;
        }

        printCancelHint();

        Employee employee = readExistingEmployee();

        System.out.println("  " + employee);

        if (readYesNo("Are you sure you want to remove " + employee.getName() + "? (y/n): ")) {
            system.removeEmployee(employee.getId());
            System.out.println("  Employee " + employee.getId() + " was removed.");
        } else {
            System.out.println("  No problem, the employee was not removed.");
        }
    }

    private static void calculateNetSalary() {

        System.out.println("\n--- Calculate Net Salary ---");

        if (!ensureEmployeesExist()) {
            return;
        }

        printCancelHint();

        Employee employee = readExistingEmployee();
        double gross = employee.getSalary();

        System.out.println("  Gross salary for " + employee.getName() + ": " + Validator.formatMoney(gross));

        double tax = readAmount("Tax amount (0 - " + Validator.formatMoney(gross) + "): ",
                "Tax", amount -> Validator.checkTax(amount, gross));

        double netSalary = system.calculateNetSalary(employee.getId(), tax);

        System.out.println("  Gross salary: " + Validator.formatMoney(gross));
        System.out.println("  Tax:          " + Validator.formatMoney(tax));
        System.out.println("  Net salary:   " + Validator.formatMoney(netSalary));
    }

    private static void generatePayrollReport() {

        System.out.println("\n--- Payroll Report ---");

        if (!ensureEmployeesExist()) {
            return;
        }

        printCancelHint();

        double taxPercentage = readAmount("Tax percentage (0 - 100): ",
                "Tax percentage", Validator::checkTaxPercentage);

        System.out.println("\n" + system.generatePayrollReport(taxPercentage));
    }

    // --------------------------------------------------
    // Input helpers
    // --------------------------------------------------

    private static void printCancelHint() {
        System.out.println("  (Type 'cancel' at any time to go back to the menu.)");
    }

    private static boolean ensureEmployeesExist() {

        if (!system.hasEmployees()) {
            System.out.println("  There are no employees registered yet. Please add an employee first (option 1).");
            return false;
        }

        return true;
    }

    // Reads one trimmed line; exits cleanly if the input stream is closed
    private static String readRawLine(String prompt) {

        System.out.print(prompt);

        if (!scanner.hasNextLine()) {
            System.out.println("\nInput closed. Goodbye!");
            System.exit(0);
        }

        return scanner.nextLine().trim();
    }

    // Like readRawLine, but supports "cancel" and rejects overly long input
    private static String readLine(String prompt) {

        while (true) {

            String input = readRawLine(prompt);

            if (input.equalsIgnoreCase("cancel")) {
                throw new CancelledException();
            }

            if (input.length() > MAX_INPUT_LENGTH) {
                System.out.println("  That input is too long (maximum " + MAX_INPUT_LENGTH
                        + " characters). Please try again.");
                continue;
            }

            return input;
        }
    }

    // Keeps asking until check returns null (no error)
    private static String readValid(String prompt, UnaryOperator<String> check) {

        while (true) {

            String input = readLine(prompt);
            String error = check.apply(input);

            if (error == null) {
                return input;
            }

            System.out.println("  " + error);
        }
    }

    private static Employee readExistingEmployee() {

        String id = readValid("Employee ID (e.g. E001): ", input -> {
            String error = Validator.checkId(input);
            if (error == null && system.findEmployee(input) == null) {
                error = "Sorry, no employee was found with ID '" + Validator.normalizeId(input)
                        + "'. Please check the ID and try again.";
            }
            return error;
        });

        return system.findEmployee(id);
    }

    // When currentValue is not null, pressing Enter keeps it
    private static String readDepartment(String currentValue) {

        String[] departments = Validator.DEPARTMENTS;

        System.out.println("  Departments:");

        for (int i = 0; i < departments.length; i++) {
            System.out.println("    " + (i + 1) + ". " + departments[i]);
        }

        String prompt = currentValue == null
                ? "Department (number or name): "
                : "New department [" + currentValue + "]: ";

        String input = readValid(prompt, value -> {
            if (value.isEmpty() && currentValue != null) {
                return null;
            }
            if (value.matches("\\d+")) {
                return value.length() <= 2
                        && Integer.parseInt(value) >= 1
                        && Integer.parseInt(value) <= departments.length
                        ? null
                        : "Please choose a department number between 1 and " + departments.length + ".";
            }
            return Validator.checkDepartment(value);
        });

        if (input.isEmpty()) {
            return currentValue;
        }

        if (input.matches("\\d+")) {
            return departments[Integer.parseInt(input) - 1];
        }

        return Validator.toDepartment(input);
    }

    private static double readSalary(String prompt, Double currentValue) {

        while (true) {

            String input = readLine(prompt);

            if (input.isEmpty() && currentValue != null) {
                return currentValue;
            }

            String error = Validator.checkAmountFormat(input, "Salary");

            if (error == null) {
                double salary = Validator.parseAmount(input);
                error = Validator.checkSalary(salary);

                if (error == null) {
                    return salary;
                }
            }

            System.out.println("  " + error);
        }
    }

    private static double readAmount(
            String prompt,
            String label,
            DoubleFunction<String> rangeCheck) {

        while (true) {

            String input = readLine(prompt);
            String error = Validator.checkAmountFormat(input, label);

            if (error == null) {
                double amount = Validator.parseAmount(input);
                error = rangeCheck.apply(amount);

                if (error == null) {
                    return amount;
                }
            }

            System.out.println("  " + error);
        }
    }

    private static boolean readYesNo(String prompt) {

        while (true) {

            String answer = readLine(prompt).toLowerCase();

            if (answer.equals("y") || answer.equals("yes")) {
                return true;
            }

            if (answer.equals("n") || answer.equals("no")) {
                return false;
            }

            System.out.println("  Please answer with 'y' for yes or 'n' for no.");
        }
    }
}
