package bg.latona.santa.reports;

import java.math.BigDecimal;

import bg.latona.santa.entities.article.Article;
import bg.latona.santa.entities.task.Task;
import lombok.Data;

@Data //auto-create getters and setters
public class PlannedIncomeOrExpenseDTO {
	private Article article;
	private BigDecimal ammount;
	private Task task;

	public PlannedIncomeOrExpenseDTO(Article article, BigDecimal ammount, Task task) {
		super();
		this.article = article;
		this.ammount = ammount;
		this.task = task;
	}
}
