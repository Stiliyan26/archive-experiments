package bg.latona.santa.entities.task;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;
import bg.latona.santa.entities.transport.Transport;
import bg.latona.santa.entities.transport.TransportOrder;
import lombok.Data;

@Data
@Audited
@Entity
public class TaskRelation extends CompanyRecord {

	@ManyToOne
	private TaskRelationType relation;
	@ManyToOne
	private Task fromTask;
	@ManyToOne
	private Task toTask;
	
	//These fields are only for the purpose of allowing the JOIN of transport order without type casting. There are special getters for these fields below.
	@JoinColumn(name="from_task_id",insertable=false,updatable=false)
	@NotAudited
	@ManyToOne
	private TransportOrder transportOrder;
	@JoinColumn(name="to_task_id",insertable=false,updatable=false)
	@NotAudited
	@ManyToOne
	private Transport transport;
	
	public TaskRelation() {}
	
	public TaskRelation(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			TaskRelationType relation, Task fromTask, Task toTask) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.relation = relation;
		this.fromTask = fromTask;
		this.toTask = toTask;
	}
	
	TransportOrder getTransportOrder() {
		if(toTask instanceof TransportOrder) {
			return (TransportOrder) fromTask;
		}
		return null;
	}
	
	Transport getTransport() {
		if(toTask instanceof Transport) {
			return (Transport) toTask;
		}
		return null;
	}
}
