package bg.latona.santa.repositories;

import bg.latona.santa.entities.wato.QImportedArticle;
import bg.latona.santa.entities.wato.ImportedArticle;

import java.util.List;

import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;

public interface ImportedArticleRepository extends CommonRepository<ImportedArticle, QImportedArticle, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<ImportedArticle> findByForeignId(@Param("foreignId") String foreignId);
}