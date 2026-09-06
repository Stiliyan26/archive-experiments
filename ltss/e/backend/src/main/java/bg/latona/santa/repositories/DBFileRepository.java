package bg.latona.santa.repositories;

import bg.latona.santa.entities.DBFile;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.QDBFile;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;
import java.util.Optional;

public interface DBFileRepository extends CommonRepository<DBFile, QDBFile, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	DBFile findFirstById(Long id);

	@RestResource(exported = false)
	List<DBFile> findByIdInAndCompanyAndDeleted(List<Long> ids, ManagedCompany company, boolean isDeleted);

	@RestResource(exported = false)
	List<DBFile> findAllByCompanyAndDeleted(ManagedCompany company, boolean isDeleted);
}