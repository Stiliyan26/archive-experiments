package bg.latona.santa.entities.task;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.hibernate.envers.Audited;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;

@Data
@Audited
@Entity
public class TaskWatcher extends CompanyRecord {

	@ManyToOne
	private Task task;
	@ManyToOne
	private SecUser watcher;
	
	public TaskWatcher() {}
	
	public TaskWatcher(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			Task task, SecUser watcher) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.task = task;
		this.watcher = watcher;
	}
}
