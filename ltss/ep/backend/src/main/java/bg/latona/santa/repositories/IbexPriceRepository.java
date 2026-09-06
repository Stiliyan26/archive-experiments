package bg.latona.santa.repositories;

import java.math.BigDecimal;
import java.time.LocalDate;

import bg.latona.santa.entities.nepal.IbexPrice;
import bg.latona.santa.entities.nepal.QIbexPrice;
import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;

public interface IbexPriceRepository extends CommonRepository<IbexPrice, QIbexPrice, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	IbexPrice findFirstByLocalDateAndHourAndCompanyAndDeleted(LocalDate localDate, BigDecimal hour, ManagedCompany company, boolean deleted);
}
