package bg.latona.santa.repositories;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.nepal.IbexEnergyDeal;
import bg.latona.santa.entities.nepal.QIbexEnergyDeal;


public interface IbexEnergyDealRepository extends CommonRepository<IbexEnergyDeal, QIbexEnergyDeal, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	IbexEnergyDeal findFirstByIdAndIsDistributedToSchedulesAndCompanyAndDeleted(Long id, boolean isDistributedToSchedules, ManagedCompany company, boolean deleted);

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<IbexEnergyDeal> findByIsDistributedToSchedulesAndCompanyAndDeleted(boolean isDistributedToSchedules, ManagedCompany company, boolean deleted);

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	IbexEnergyDeal findFirstByTradeIdAndCompanyAndDeleted(String tradeId, ManagedCompany company, boolean deleted);

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<IbexEnergyDeal> findFirst200ByIsDistributedToSchedulesAndCompanyAndDeletedOrderByCreatedDateDesc(boolean isDistributedToSchedules, ManagedCompany company, boolean deleted);

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<IbexEnergyDeal> findByCompanyAndDeleted(ManagedCompany company, boolean deleted);

	@Query("SELECT d FROM IbexEnergyDeal d WHERE d.createdDate >= :last24Hours AND d.company = :company AND d.deleted = :deleted ORDER BY d.createdDate DESC")
	List<IbexEnergyDeal> findDealsFromLast24Hours(
			@Param("last24Hours") Date last24Hours,
			@Param("company") ManagedCompany company,
			@Param("deleted") Boolean deleted);

}
