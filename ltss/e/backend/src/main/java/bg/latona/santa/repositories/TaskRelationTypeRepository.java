package bg.latona.santa.repositories;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.task.QTaskRelationType;
import bg.latona.santa.entities.task.TaskRelationType;

public interface TaskRelationTypeRepository extends CommonRepository<TaskRelationType, QTaskRelationType, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	TaskRelationType findFirstByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted);
}