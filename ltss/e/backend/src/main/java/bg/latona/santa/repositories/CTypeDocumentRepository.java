package bg.latona.santa.repositories;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.CTypeDocument;
import bg.latona.santa.entities.santa.common.QCTypeDocument;

public interface CTypeDocumentRepository extends CommonRepository<CTypeDocument, QCTypeDocument, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	CTypeDocument findFirstByShortNameAndCompanyAndDeleted(String shortName, ManagedCompany company, boolean deleted);
}
