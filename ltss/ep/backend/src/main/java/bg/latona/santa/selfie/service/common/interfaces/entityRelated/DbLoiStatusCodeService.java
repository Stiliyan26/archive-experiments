package bg.latona.santa.selfie.service.common.interfaces.entityRelated;

import bg.latona.santa.entities.selfie.LoiStatusCode;

public interface DbLoiStatusCodeService {

   LoiStatusCode getLoiStatusCodeServiceByListOptionItemCode(Long code);
}