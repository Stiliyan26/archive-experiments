package bg.latona.santa.selfie.service.common.interfaces.entityRelated;

import bg.latona.santa.entities.selfie.LoiReasonForTermination;

public interface DbLoiReasonForTerminationService {

    LoiReasonForTermination getLoiReasonForTerminationByListOptionItemCode(Long code);
}
