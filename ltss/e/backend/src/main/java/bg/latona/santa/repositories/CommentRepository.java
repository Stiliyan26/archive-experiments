package bg.latona.santa.repositories;

import bg.latona.santa.entities.task.QComment;
import bg.latona.santa.entities.task.Comment;

public interface CommentRepository extends CommonRepository<Comment, QComment, Long> {
	
}