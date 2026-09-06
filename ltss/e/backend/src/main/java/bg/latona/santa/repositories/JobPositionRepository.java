package bg.latona.santa.repositories;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.employee.QJobPosition;
import bg.latona.santa.entities.employee.JobPosition;

public interface JobPositionRepository extends CommonRepository<JobPosition, QJobPosition, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	JobPosition findFirstByCodeAndCompanyAndDeleted(String code, ManagedCompany company, boolean deleted);
}