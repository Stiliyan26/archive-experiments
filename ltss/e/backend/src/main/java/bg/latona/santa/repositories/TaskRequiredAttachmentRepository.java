package bg.latona.santa.repositories;

import java.util.List;

import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.task.QTaskRequiredAttachment;
import bg.latona.santa.entities.task.Task;
import bg.latona.santa.entities.task.TaskRequiredAttachment;

public interface TaskRequiredAttachmentRepository extends CommonRepository<TaskRequiredAttachment, QTaskRequiredAttachment, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<TaskRequiredAttachment> findByTask(@Param("task") Task task);
}