package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.finance.LoiBatchTteLinkStatus;
import bg.latona.santa.entities.santa.finance.QLoiBatchTteLinkStatus;
import org.springframework.data.rest.core.annotation.RestResource;

public interface LoiBatchTteLinkStatusRepository extends CommonRepository<LoiBatchTteLinkStatus, QLoiBatchTteLinkStatus, Long>{

    @RestResource(exported = false) //don't expose methods that are not checking permissions
    LoiBatchTteLinkStatus findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
}
