package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.person.LegalPerson;
import bg.latona.santa.entities.person.QLegalPerson;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;
import java.util.Set;

public interface LegalPersonRepository extends CommonRepository<LegalPerson, QLegalPerson, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<LegalPerson> findByEik(@Param("eik") String eik);


	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<LegalPerson> findByVatNumberAndCompany(String vatNumber, ManagedCompany company);


	@RestResource(exported = false) //don't expose methods that are not checking permissions
	LegalPerson findFirstByNameAndCompanyAndDeleted(String name, ManagedCompany company, boolean deleted);


	@RestResource(exported = false) //don't expose methods that are not checking permissions
	LegalPerson findFirstByEikAndCompanyAndDeleted(String eik, ManagedCompany company, boolean deleted);


	@RestResource(exported = false)
	Set<LegalPerson> findDistinctByPowerPlantsAccessPointInAndCompanyAndDeleted(
			Set<String> accessPoints, ManagedCompany company, boolean deleted
	);


	@RestResource(exported = false)
    LegalPerson findFirstByEgnAndCompanyAndDeleted(String egn, ManagedCompany company, boolean deleted);


	@RestResource(exported = false)
	LegalPerson findFirstByIsSenderAndCompanyAndDeleted(boolean isSender, ManagedCompany company, boolean deleted);


	@RestResource(exported = false)
	Set<LegalPerson> findAllByCompanyAndDeleted(ManagedCompany company, boolean deleted);
}