package bg.latona.santa.selfie.service.common.interfaces.entityRelated;

import bg.latona.santa.entities.selfie.LoiAgreementStatus;

public interface DbLoiAgreementStatusService {

   LoiAgreementStatus getLoiAgreementStatusByListOptionItemCode(Long code);
}