package bg.latona.santa.repositories;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ListOptionItem;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.QListOptionItem;

public interface ListOptionItemRepository extends CommonRepository<ListOptionItem, QListOptionItem, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	ListOptionItem findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
}