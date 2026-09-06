package bg.latona.santa.repositories;

import java.util.List;

import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.task.QTaskAttachment;
import bg.latona.santa.entities.task.Task;
import bg.latona.santa.entities.task.TaskAttachment;

public interface TaskAttachmentRepository extends CommonRepository<TaskAttachment, QTaskAttachment, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<TaskAttachment> findByTask(@Param("task") Task task);
}