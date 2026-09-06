package bg.latona.santa.repositories;

import bg.latona.santa.entities.wato.QImportedLegalPerson;
import bg.latona.santa.entities.wato.ImportedLegalPerson;

import java.util.List;

import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;

public interface ImportedLegalPersonRepository extends CommonRepository<ImportedLegalPerson, QImportedLegalPerson, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<ImportedLegalPerson> findByForeignId(@Param("foreignId") String foreignId);
}