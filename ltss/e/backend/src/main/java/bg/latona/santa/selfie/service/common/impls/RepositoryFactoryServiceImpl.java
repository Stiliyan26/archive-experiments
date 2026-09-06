package bg.latona.santa.selfie.service.common.impls;

import bg.latona.santa.selfie.service.common.interfaces.RepositoryFactoryService;
import bg.latona.santa.reports.ReportException;
import bg.latona.santa.repositories.CommonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.support.Repositories;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

@RequiredArgsConstructor
@Service
public class RepositoryFactoryServiceImpl implements RepositoryFactoryService {

    private Repositories repositories = null;

    @Autowired
    private final WebApplicationContext appContext;

    private Repositories getRepositories() {
        if (repositories == null) {
            repositories = new Repositories(appContext);
        }
        return repositories;
    }

    @Override
    public <T, R extends CommonRepository<T, ?, ?>> R getRepository(Class<R> repositoryClass, Class<T> entityClass) {
        return repositoryClass.cast(getRepositories().getRepositoryFor(entityClass)
                .orElseThrow(() -> new ReportException("No repository found for " + entityClass.getName())));
    }
}
