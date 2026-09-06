package bg.latona.santa.repositories;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.employee.QJobRequirement;
import bg.latona.santa.entities.employee.JobRequirement;

public interface JobRequirementRepository extends CommonRepository<JobRequirement, QJobRequirement, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	JobRequirement findFirstByCodeAndCompanyAndDeleted(String code, ManagedCompany company, boolean deleted);
}