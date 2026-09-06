package bg.latona.santa.repositories;

import bg.latona.santa.entities.allocation.QAllocationProxy;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.allocation.AllocationOrigin;
import bg.latona.santa.entities.allocation.AllocationProxy;
import bg.latona.santa.entities.allocation.AllocationType;

public interface AllocationProxyRepository extends CommonRepository<AllocationProxy, QAllocationProxy, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	AllocationProxy findFirstByAllocationOriginAndAllocationTypeAndCompanyAndDeleted(AllocationOrigin allocationOrigin,
			AllocationType allocationType, ManagedCompany company, boolean deleted);

}