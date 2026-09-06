package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.selfie.AgreementTypeMapping;
import bg.latona.santa.entities.selfie.QAgreementTypeMapping;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.rest.core.annotation.RestResource;

import java.math.BigDecimal;
import java.util.Set;


public interface AgreementTypeMappingRepository extends CommonRepository<AgreementTypeMapping, QAgreementTypeMapping, Long>, JpaSpecificationExecutor<AgreementTypeMapping> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	Set<AgreementTypeMapping> findByCompanyAndDeleted(ManagedCompany company, boolean deleted);

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	AgreementTypeMapping findFirstByCrmCodeAndCompanyAndDeleted(BigDecimal crmCode, ManagedCompany company, boolean deleted);
}
