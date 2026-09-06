package bg.latona.santa.repositories;

import bg.latona.santa.entities.employee.QEmployee;
import bg.latona.santa.entities.employee.Employee;

public interface EmployeeRepository extends CommonRepository<Employee, QEmployee, Long> {
	
}