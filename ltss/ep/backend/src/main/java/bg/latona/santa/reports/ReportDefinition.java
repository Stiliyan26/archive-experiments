package bg.latona.santa.reports;

import lombok.Data;

@Data //auto-create getters and setters
public class ReportDefinition {
	public String from;
	public String leftJoin;
	public String joinPath;
}
