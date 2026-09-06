package bg.latona.santa.repositories;

import java.util.List;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.finance.FPtBatch;
import bg.latona.santa.entities.santa.finance.FPtBatchCcDetail;
import bg.latona.santa.entities.santa.finance.LoiPtBatchCcDetailStatus;
import bg.latona.santa.entities.santa.finance.QFPtBatchCcDetail;

public interface FPtBatchCcDetailRepository extends CommonRepository<FPtBatchCcDetail, QFPtBatchCcDetail, Long>{
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<FPtBatchCcDetail> findByBahIdAndStatusAndCompanyAndDeleted(FPtBatch bahId, LoiPtBatchCcDetailStatus status, ManagedCompany company, boolean deleted);
}
