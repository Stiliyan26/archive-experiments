package bg.latona.santa.repositories;

import java.util.List;

import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.task.Task;
import bg.latona.santa.entities.task.TimeSheetItem;
import bg.latona.santa.entities.task.QTimeSheetItem;

public interface TimeSheetItemRepository extends CommonRepository<TimeSheetItem, QTimeSheetItem, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<TimeSheetItem> findByTask(@Param("task") Task task);
}