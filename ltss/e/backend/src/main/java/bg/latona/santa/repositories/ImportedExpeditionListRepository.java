package bg.latona.santa.repositories;

import bg.latona.santa.entities.wato.QImportedExpeditionList;
import bg.latona.santa.entities.wato.ImportedExpeditionList;
import bg.latona.santa.entities.wato.ImportedOrder;

import java.util.List;

import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.lang.Nullable;

public interface ImportedExpeditionListRepository extends CommonRepository<ImportedExpeditionList, QImportedExpeditionList, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<ImportedExpeditionList> findByForeignId(String foreignId);
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<ImportedExpeditionList> findByForeignOrderIdAndOrder(String foreignOrderId, @Nullable ImportedOrder order);
}