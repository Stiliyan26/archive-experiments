package bg.latona.santa.selfie.service.common.interfaces;

import bg.latona.santa.entities.CommonRecord;

public interface EntityPersistenceService {

    CommonRecord persistEntity(CommonRecord entity, boolean doReturnNullAtErrors, boolean doFlush);
}
