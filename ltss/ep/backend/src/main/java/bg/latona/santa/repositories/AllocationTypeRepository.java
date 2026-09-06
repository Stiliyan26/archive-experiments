package bg.latona.santa.repositories;

import java.util.List;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.allocation.QAllocationType;
import bg.latona.santa.entities.allocation.AllocationType;

public interface AllocationTypeRepository extends CommonRepository<AllocationType, QAllocationType, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	AllocationType findFirstByNameAndCompanyAndDeleted(String name, ManagedCompany company, boolean deleted);
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	AllocationType findFirstByProducerAndConsumerAndCompanyAndDeleted(String producer, String consumer, ManagedCompany company, boolean deleted);
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<AllocationType> findByProducerAndCompanyAndDeleted(String producer, ManagedCompany company, boolean deleted);
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<AllocationType> findByConsumerAndCompanyAndDeleted(String consumer, ManagedCompany company, boolean deleted);
}