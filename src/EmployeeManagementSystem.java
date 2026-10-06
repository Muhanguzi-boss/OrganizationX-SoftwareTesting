import java.util.ArrayList;
import java.util.List;

public class EmployeeManagementSystem {

    private List<Employee> employees = new ArrayList<>();

    // Add employee
    public boolean addEmployee(Employee employee) {

        if (employee == null) {
            return false;
        }

        if (findEmployee(employee.getId()) != null) {
            return false;
        }

        employees.add(employee);
        return true;
    }

    public boolean hasEmployees() {
        return !employees.isEmpty();
    }

    // Find employee
    public Employee findEmployee(String id) {

        if (Validator.checkId(id) != null) {
            return null;
        }

        String normalizedId = Validator.normalizeId(id);

        for (Employee employee : employees) {

            if (employee.getId().equals(normalizedId)) {
                return employee;
            }
        }

        return null;
    }

    // Update employee
    public boolean updateEmployee(
            String id,
            String name,
            String department,
            double salary) {

        Employee employee = findEmployee(id);

        if (employee == null) {
            return false;
        }

        employee.updateDetails(
                name,
                department,
                salary
        );

        return true;
    }

    // Remove employee
    public boolean removeEmployee(String id) {

        Employee employee = findEmployee(id);

        if (employee == null) {
            return false;
        }

        employees.remove(employee);
        return true;
    }

    // Calculate net salary
    public double calculateNetSalary(
            String id,
            double tax) {

        Employee employee = findEmployee(id);

        if (employee == null) {
            throw new IllegalArgumentException(
                    "Employee not found"
            );
        }

        String taxError =
                Validator.checkTax(tax, employee.getSalary());

        if (taxError != null) {
            throw new IllegalArgumentException(taxError);
        }

        return employee.getSalary() - tax;
    }

    // Generate employee report
    public String generateEmployeeReport() {

        StringBuilder report = new StringBuilder();

        report.append("EMPLOYEE REPORT\n");

        if (employees.isEmpty()) {
            report.append("No employees registered.\n");
            return report.toString();
        }

        for (Employee employee : employees) {
            report.append(employee).append("\n");
        }

        return report.toString();
    }

    // Generate payroll report
    public String generatePayrollReport(
            double taxPercentage) {

        String percentageError =
                Validator.checkTaxPercentage(taxPercentage);

        if (percentageError != null) {
            throw new IllegalArgumentException(percentageError);
        }

        StringBuilder report = new StringBuilder();

        report.append("PAYROLL REPORT\n");

        for (Employee employee : employees) {

            double tax =
                    employee.getSalary()
                            * taxPercentage / 100;

            double netSalary =
                    employee.getSalary() - tax;

            report.append(employee.getId())
                    .append(" - ")
                    .append(employee.getName())
                    .append(" - Net Salary: ")
                    .append(Validator.formatMoney(netSalary))
                    .append("\n");
        }

        return report.toString();
    }
}