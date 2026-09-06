package bg.latona.santa.repositories;


import bg.latona.santa.entities.selfie.AgreementType;
import bg.latona.santa.entities.selfie.QAgreementType;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;


public interface AgreementTypeRepository extends CommonRepository<AgreementType, QAgreementType, Long>, JpaSpecificationExecutor<AgreementType> {

    List<AgreementType> findAllByIsValidIsTrue();
}
