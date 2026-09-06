package bg.latona.santa.repositories;

import bg.latona.santa.entities.wato.QImportedWarehouseStock;
import bg.latona.santa.entities.wato.ImportedWarehouseStock;

import java.util.List;

import org.springframework.data.rest.core.annotation.RestResource;

public interface ImportedWarehouseStockRepository extends CommonRepository<ImportedWarehouseStock, QImportedWarehouseStock, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<ImportedWarehouseStock> findByForeignArticleId(String articleId);
}