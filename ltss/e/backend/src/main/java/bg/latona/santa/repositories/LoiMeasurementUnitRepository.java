package bg.latona.santa.repositories;

import bg.latona.santa.entities.selfie.LoiMeasurementUnit;
import bg.latona.santa.entities.selfie.QLoiMeasurementUnit;
import bg.latona.santa.entities.ManagedCompany;
import org.springframework.data.rest.core.annotation.RestResource;


public interface LoiMeasurementUnitRepository extends CommonRepository<LoiMeasurementUnit, QLoiMeasurementUnit, Long> {

    @RestResource(exported = false) //don't expose methods that are not checking permissions
    LoiMeasurementUnit findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
}
