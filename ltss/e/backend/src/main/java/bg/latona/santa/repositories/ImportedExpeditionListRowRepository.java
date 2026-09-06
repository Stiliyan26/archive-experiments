package bg.latona.santa.repositories;

import bg.latona.santa.entities.wato.QImportedExpeditionListRow;
import bg.latona.santa.entities.wato.ImportedExpeditionList;
import bg.latona.santa.entities.wato.ImportedExpeditionListRow;
import bg.latona.santa.entities.wato.ImportedOrderRow;

import java.util.List;

import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.lang.Nullable;

public interface ImportedExpeditionListRowRepository extends CommonRepository<ImportedExpeditionListRow, QImportedExpeditionListRow, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<ImportedExpeditionListRow> findByForeignExpeditionListIdAndExpeditionList(String foreignExpeditionListId, @Nullable ImportedExpeditionList expeditionList);
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<ImportedExpeditionListRow> findByForeignOrderRowIdAndOrderRow(String foreignOrderRowId, @Nullable ImportedOrderRow orderRow);
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<ImportedExpeditionListRow> findByOrderRow(ImportedOrderRow orderRow);
}