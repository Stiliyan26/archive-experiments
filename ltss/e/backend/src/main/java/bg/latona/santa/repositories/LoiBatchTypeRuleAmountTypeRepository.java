package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.finance.LoiBatchTypeRuleAmountType;
import bg.latona.santa.entities.santa.finance.QLoiBatchTypeRuleAmountType;
import org.springframework.data.rest.core.annotation.RestResource;

public interface LoiBatchTypeRuleAmountTypeRepository extends CommonRepository<LoiBatchTypeRuleAmountType, QLoiBatchTypeRuleAmountType, Long>{

    @RestResource(exported = false) //don't expose methods that are not checking permissions
    LoiBatchTypeRuleAmountType findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
}
