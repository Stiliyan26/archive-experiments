package bg.latona.santa.repositories;

import bg.latona.santa.entities.task.QTask;
import bg.latona.santa.entities.task.Task;

public interface TaskRepository extends CommonRepository<Task, QTask, Long> {
	
}