package bg.latona.santa.entities;

import java.util.Date;
import java.util.List;

import javax.persistence.*;

import bg.latona.santa.entities.employee.EmployeeCompetence;
import bg.latona.santa.entities.selfie.ElectricityInvoice;
import bg.latona.santa.entities.selfie.SapXml;
import bg.latona.santa.entities.mail.MailAttachment;
import bg.latona.santa.entities.nepal.Schedule;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.Audited;

import bg.latona.santa.entities.security.SecUser;
import lombok.Data;

@Data //auto-create getters and setters
@ToString(exclude = {"employeeCompetences", "mailAttachments", "schedules", "electricityInvoices", "sapXmls"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"employeeCompetences", "mailAttachments", "schedules", "electricityInvoices", "sapXmls"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class DBFile extends Attachable {
	//TODO check if the content is too slow to transfer through JSON: https://spring.io/guides/gs/uploading-files/
	private byte[] content;
	private String contentType;


	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "document")
	private List<EmployeeCompetence> employeeCompetences;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "attachment")
	private List<MailAttachment> mailAttachments;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "dbFile")
	private List<Schedule> schedules;

	@JsonIgnore
	@OneToMany(mappedBy = "dbFile")
	private List<ElectricityInvoice> electricityInvoices;

	@JsonIgnore
	@OneToMany(mappedBy = "dbFile")
	private List<SapXml> sapXmls;

	public DBFile() {};
	
	public DBFile(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			String name, List<HashTag> hashTags, 
			byte[] content, String contentType) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, name, hashTags);
		this.content = content;
		this.contentType = contentType;
	}
}
