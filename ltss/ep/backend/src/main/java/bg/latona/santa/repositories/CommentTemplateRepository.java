package bg.latona.santa.repositories;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.CommentTemplate;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.QCommentTemplate;

public interface CommentTemplateRepository extends CommonRepository<CommentTemplate, QCommentTemplate, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	CommentTemplate findFirstByCodeAndCompanyAndDeleted(String code, ManagedCompany company, boolean deleted);
}