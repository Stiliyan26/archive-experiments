package bg.latona.santa.repositories;

import java.util.List;

import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.security.SecUser;
import bg.latona.santa.entities.task.Task;
import bg.latona.santa.entities.task.TaskWatcher;
import bg.latona.santa.entities.task.QTaskWatcher;

public interface TaskWatcherRepository extends CommonRepository<TaskWatcher, QTaskWatcher, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<TaskWatcher> findByTask(@Param("task") Task task);
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<TaskWatcher> findByTaskAndWatcher(@Param("task") Task task, @Param("watcher") SecUser watcher);
}