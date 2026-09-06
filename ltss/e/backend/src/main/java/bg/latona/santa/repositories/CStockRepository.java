package bg.latona.santa.repositories;

import java.util.List;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import bg.latona.santa.entities.santa.common.CGoods;
import bg.latona.santa.entities.santa.common.CStock;
import bg.latona.santa.entities.santa.common.QCStock;

public interface CStockRepository extends CommonRepository<CStock, QCStock, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<CStock> findByGodIdAndOutCodeAndCompanyAndDeleted(CGoods godId, CCcOrganizationUnit outCode, ManagedCompany company, boolean deleted);
}
