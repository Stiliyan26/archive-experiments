package bg.latona.santa.repositories;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.transport.QShippingContainerType;
import bg.latona.santa.entities.transport.ShippingContainerType;

public interface ShippingContainerTypeRepository extends CommonRepository<ShippingContainerType, QShippingContainerType, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	ShippingContainerType findFirstByCodeAndCompanyAndDeleted(String code, ManagedCompany company, boolean deleted);
}