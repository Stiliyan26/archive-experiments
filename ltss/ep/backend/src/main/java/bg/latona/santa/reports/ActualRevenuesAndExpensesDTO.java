package bg.latona.santa.reports;

import java.math.BigDecimal;

import bg.latona.santa.entities.article.Article;
import bg.latona.santa.entities.task.Task;
import lombok.Data;

@Data //auto-create getters and setters
public class ActualRevenuesAndExpensesDTO {
	private Article article;
	private BigDecimal ammount;
	private Task task;
	private String description;
	
	public ActualRevenuesAndExpensesDTO(Article article, BigDecimal ammount, Task task, String description) {
		super();
		this.article = article;
		this.ammount = ammount;
		this.task = task;
		this.description = description;
	}
}
