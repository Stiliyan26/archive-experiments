package bg.latona.santa.selfie.service.common.impls;

import bg.latona.santa.DroolsRuleException;
import bg.latona.santa.selfie.service.common.interfaces.EntityPersistenceService;
import bg.latona.santa.selfie.service.common.interfaces.RepositoryFactoryService;
import bg.latona.santa.RepositoryConfiguration;
import bg.latona.santa.entities.CommonRecord;
import bg.latona.santa.repositories.CommonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@RequiredArgsConstructor
@Service
public class EntityPersistenceServiceImpl implements EntityPersistenceService {

    private final RepositoryFactoryService repositoryFactoryService;

    @Override
    public CommonRecord persistEntity(CommonRecord entity, boolean doReturnNullAtErrors, boolean doFlush) {
        log.trace("persistEntity " + entity);

        CommonRepository repo = repositoryFactoryService.getRepository(CommonRepository.class, entity.getClass());

        try {
            validateBeforeSave(entity);

            CommonRecord result = doFlush
                    ? saveAndFlushEntity(repo, entity)
                    : saveEntity(repo, entity);

            validateAfterSave(entity);

            return result;

        } catch (DroolsRuleException e) {
            return handleDroolsRuleException(e, entity, doReturnNullAtErrors);

        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.error("Data integrity violation while saving entity: {}", e.getMessage());
            return null;
        }
    }

    private void validateBeforeSave(CommonRecord entity) {
        if (entity.getId() == null) {
            RepositoryConfiguration.getBeforeCreateValidator().validate(entity, null);
        } else {
            RepositoryConfiguration.getBeforeSaveValidator().validate(entity, null);
        }
    }

    private void validateAfterSave(CommonRecord entity) {
        if (entity.getId() == null) {
            RepositoryConfiguration.getAfterCreateValidator().validate(entity, null);
        } else {
            RepositoryConfiguration.getAfterSaveValidator().validate(entity, null);
        }
    }

    private CommonRecord saveEntity(CommonRepository<CommonRecord, ?, ?> repo, CommonRecord entity) {
        return repo.save(entity);
    }

    private CommonRecord saveAndFlushEntity(CommonRepository<CommonRecord, ?, ?> repo, CommonRecord entity) {
        return repo.saveAndFlush(entity);
    }

    private CommonRecord handleDroolsRuleException(DroolsRuleException e, CommonRecord entity, boolean doReturnNullAtErrors) {
        if (!doReturnNullAtErrors && e.getResult().getFieldErrors().size() > 0
                && e.getResult().getFieldErrors().get(0).getCodes()[0].equals("notUnique")) {
            return (CommonRecord) e.getResult().getFieldErrors().get(0).getRejectedValue();
        } else {
            log.info(entity.getClass().getSimpleName() + ": " + e.getMessage());
            return null;
        }
    }
}
