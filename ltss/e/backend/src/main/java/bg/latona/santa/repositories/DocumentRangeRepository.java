package bg.latona.santa.repositories;


import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.selfie.DocumentRange;
import bg.latona.santa.entities.selfie.QDocumentRange;
import org.springframework.data.rest.core.annotation.RestResource;


public interface DocumentRangeRepository extends CommonRepository<DocumentRange, QDocumentRange, Long> {

    @RestResource(exported = false)
    DocumentRange findFirstByRangeFromAndRangeToAndCompanyAndDeleted(Integer periodFrom, Integer periodTo, ManagedCompany company, boolean deleted);
}
