package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.nepal.PowerPlant;
import bg.latona.santa.entities.nepal.QPowerPlant;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;
import java.util.Optional;

public interface PowerPlantRepository extends CommonRepository<PowerPlant, QPowerPlant, Long>{

	@RestResource(exported = false)
	List<PowerPlant> findByCompanyAndDeleted(ManagedCompany company, boolean deleted);

	@RestResource(exported = false)
	PowerPlant findFirstByProducerEicAndCompanyAndDeleted(String producerEic, ManagedCompany company, boolean deleted);


	@RestResource(exported = false)
	List<PowerPlant> findAllByAccessPointAndCompanyAndDeleted(String accessPoint , ManagedCompany company, boolean deleted);


	@RestResource(exported = false)
	PowerPlant findFirstByAccessPointAndCompanyAndDeleted(String accessPoint , ManagedCompany company, boolean deleted);


	@RestResource(exported = false)
	List<PowerPlant> findAllByAccessPointInAndCompanyAndDeleted(List<String> accessPoints, ManagedCompany company, boolean deleted);


	@RestResource(exported = false)
    Optional<PowerPlant> findByAccessPointAndCompanyAndDeleted(String accessPoint, ManagedCompany company, boolean deleted);


	@RestResource(exported = false)
	PowerPlant findFirstByIdAndCompanyAndDeleted(Long id, ManagedCompany company, boolean deleted);
}
