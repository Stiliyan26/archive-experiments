package bg.latona.santa.repositories;

import bg.latona.santa.entities.task.QPlannedIncomeOrExpense;
import bg.latona.santa.entities.task.PlannedIncomeOrExpense;
import bg.latona.santa.entities.task.Task;

import java.util.List;

import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;

public interface PlannedIncomeOrExpenseRepository extends CommonRepository<PlannedIncomeOrExpense, QPlannedIncomeOrExpense, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<PlannedIncomeOrExpense> findByTask(@Param("task") Task task);
}