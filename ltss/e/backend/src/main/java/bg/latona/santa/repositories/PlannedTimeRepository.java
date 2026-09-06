package bg.latona.santa.repositories;

import bg.latona.santa.entities.task.QPlannedTime;
import bg.latona.santa.entities.task.PlannedTime;
import bg.latona.santa.entities.task.Task;

import java.util.List;

import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;

public interface PlannedTimeRepository extends CommonRepository<PlannedTime, QPlannedTime, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<PlannedTime> findByTask(@Param("task") Task task);
}