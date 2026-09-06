package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.finance.LoiRepresentativeType;
import bg.latona.santa.entities.santa.finance.QLoiRepresentativeType;
import org.springframework.data.rest.core.annotation.RestResource;

public interface LoiRepresentativeTypeRepository extends CommonRepository<LoiRepresentativeType, QLoiRepresentativeType, Long>{

    @RestResource(exported = false) //don't expose methods that are not checking permissions
    LoiRepresentativeType findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
}
