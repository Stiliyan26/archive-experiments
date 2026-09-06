package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.finance.LoiBatchJteDefaultSide;
import bg.latona.santa.entities.santa.finance.QLoiBatchJteDefaultSide;
import org.springframework.data.rest.core.annotation.RestResource;

public interface LoiBatchJteDefaultSideRepository extends CommonRepository<LoiBatchJteDefaultSide, QLoiBatchJteDefaultSide, Long>{

    @RestResource(exported = false) //don't expose methods that are not checking permissions
    LoiBatchJteDefaultSide findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
}
