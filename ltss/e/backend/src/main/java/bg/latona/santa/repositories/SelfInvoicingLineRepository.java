package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.selfie.SelfInvoicingLine;
import bg.latona.santa.entities.selfie.QSelfInvoicingLine;
import org.springframework.data.rest.core.annotation.RestResource;

public interface SelfInvoicingLineRepository extends CommonRepository<SelfInvoicingLine, QSelfInvoicingLine, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	SelfInvoicingLine findFirstBySelfInvoicingLineIdCodeAndCompanyAndDeleted(String selfInvoicingLineIdCode, ManagedCompany company, boolean deleted);
}
