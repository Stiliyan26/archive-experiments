package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.selfie.LoiDocumentType;
import bg.latona.santa.entities.selfie.QLoiDocumentType;
import org.springframework.data.rest.core.annotation.RestResource;


public interface LoiDocumentTypeRepository extends CommonRepository<LoiDocumentType, QLoiDocumentType, Long> {

    @RestResource(exported = false) //don't expose methods that are not checking permissions
    LoiDocumentType findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
}
