package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.nepal.LoiMaxLoadWeather;
import bg.latona.santa.entities.nepal.QLoiMaxLoadWeather;
import org.springframework.data.rest.core.annotation.RestResource;

public interface LoiMaxLoadWeatherRepository extends CommonRepository<LoiMaxLoadWeather, QLoiMaxLoadWeather, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	LoiMaxLoadWeather findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
}
