package bg.latona.santa.repositories;

import java.util.List;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.CMeasure;
import bg.latona.santa.entities.santa.common.QCMeasure;

public interface CMeasureRepository extends CommonRepository<CMeasure, QCMeasure, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	CMeasure findFirstByCodeAndCompanyAndDeleted(String code, ManagedCompany company, boolean deleted);
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<CMeasure> findByMeeIdAndCompanyAndDeleted(CMeasure meeId, ManagedCompany company, boolean deleted);
}
