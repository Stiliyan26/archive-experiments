package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.finance.LoiInvDdsFileStatus;
import bg.latona.santa.entities.santa.finance.QLoiInvDdsFileStatus;
import org.springframework.data.rest.core.annotation.RestResource;

public interface LoiInvDdsFileStatusRepository extends CommonRepository<LoiInvDdsFileStatus, QLoiInvDdsFileStatus, Long>{

    @RestResource(exported = false) //don't expose methods that are not checking permissions
    LoiInvDdsFileStatus findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
}
