package bg.latona.santa.repositories;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.task.QTaskPriority;
import bg.latona.santa.entities.task.TaskPriority;

public interface TaskPriorityRepository extends CommonRepository<TaskPriority, QTaskPriority, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	TaskPriority findFirstByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted);
}