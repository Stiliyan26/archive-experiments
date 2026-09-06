package bg.latona.santa.repositories;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.task.QTaskStatus;
import bg.latona.santa.entities.task.TaskStatus;

public interface TaskStatusRepository extends CommonRepository<TaskStatus, QTaskStatus, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	TaskStatus findFirstByCodeAndCompany(Long code, ManagedCompany company);
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	TaskStatus findFirstByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted);
}