package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import bg.latona.santa.entities.santa.common.CCcPartner;
import bg.latona.santa.entities.santa.finance.FCtBatchType;
import bg.latona.santa.entities.santa.finance.FPtBatch;
import bg.latona.santa.entities.santa.finance.QFPtBatch;

import java.time.LocalDate;

import org.springframework.data.rest.core.annotation.RestResource;

public interface FPtBatchRepository extends CommonRepository<FPtBatch, QFPtBatch, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	FPtBatch findFirstByIdAndCompanyAndDeleted(Long id, ManagedCompany company, boolean deleted);
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	FPtBatch findFirstByPostDateAndOutCodeAndRefNoAndBteIdAndParIdAndCompanyAndDeleted(LocalDate postDate,
			CCcOrganizationUnit outCode, String refNo, FCtBatchType bteId, CCcPartner parId, ManagedCompany company,
			boolean deleted);
}
