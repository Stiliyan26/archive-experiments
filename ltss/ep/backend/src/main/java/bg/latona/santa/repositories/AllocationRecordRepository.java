package bg.latona.santa.repositories;

import bg.latona.santa.entities.allocation.QAllocationRecord;

import java.util.List;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.allocation.AllocationProxy;
import bg.latona.santa.entities.allocation.AllocationRecord;

public interface AllocationRecordRepository extends CommonRepository<AllocationRecord, QAllocationRecord, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	AllocationRecord findFirstByAllocationProducerAndAllocationConsumerAndCompanyAndDeleted(
			AllocationProxy allocationProducer, AllocationProxy allocationConsumer,
			ManagedCompany company, boolean deleted);
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<AllocationRecord> findByAllocationConsumerAndCompanyAndDeleted(AllocationProxy allocationConsumer,
			ManagedCompany company, boolean deleted);
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<AllocationRecord> findByAllocationProducerAndCompanyAndDeleted(AllocationProxy allocationProducer,
			ManagedCompany company, boolean deleted);
}