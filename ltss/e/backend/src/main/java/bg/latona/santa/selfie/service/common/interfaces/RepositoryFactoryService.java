package bg.latona.santa.selfie.service.common.interfaces;

import bg.latona.santa.repositories.CommonRepository;

public interface RepositoryFactoryService {

    <T, R extends CommonRepository<T, ?, ?>> R getRepository(Class<R> repositoryClass, Class<T> entityClass);
}
