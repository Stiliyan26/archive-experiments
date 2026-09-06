package bg.latona.santa.repositories;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.task.QTaskType;
import bg.latona.santa.entities.task.TaskType;

public interface TaskTypeRepository extends CommonRepository<TaskType, QTaskType, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	TaskType findFirstByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted);
}