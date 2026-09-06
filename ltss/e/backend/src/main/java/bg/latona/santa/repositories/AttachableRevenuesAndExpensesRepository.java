package bg.latona.santa.repositories;

import bg.latona.santa.entities.Attachable;
import bg.latona.santa.entities.AttachableRevenuesAndExpenses;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.QAttachableRevenuesAndExpenses;
import bg.latona.santa.entities.article.Article;

import java.util.List;

import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;

public interface AttachableRevenuesAndExpensesRepository extends CommonRepository<AttachableRevenuesAndExpenses, QAttachableRevenuesAndExpenses, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<AttachableRevenuesAndExpenses> findByAttachable(@Param("attachable") Attachable attachable);
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<AttachableRevenuesAndExpenses> findByArticleAndAttachableAndCompanyAndDeleted(Article article, Attachable attachable, ManagedCompany company, boolean deleted);
}