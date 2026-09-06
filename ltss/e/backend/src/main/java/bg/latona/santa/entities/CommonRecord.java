package bg.latona.santa.entities;

import java.util.Date;

import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import bg.latona.santa.entities.security.SecUser;
import lombok.Data;

@MappedSuperclass
@Audited
@Data
public class CommonRecord {
	private @Id @GeneratedValue Long id; //auto-increment ID

	@ManyToOne(fetch = FetchType.LAZY)
	private SecUser createdBy; //MappedSuperclass cannot have OneToMany relation - make it in every descendant class if needed
	private Date createdDate;
	@ManyToOne(fetch = FetchType.LAZY)
	private SecUser lastModifiedBy; //MappedSuperclass cannot have OneToMany relation - make it in every descendant class if needed
	private Date lastModifiedDate;
	private boolean deleted;
	//don't save in DB, just use it as business rules parameter
	@Transient
	private boolean calculateOnly;

	public CommonRecord() {}
	
	public CommonRecord(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly) {
		super();
		this.createdBy = createdBy;
		this.createdDate = createdDate;
		this.lastModifiedBy = lastModifiedBy;
		this.lastModifiedDate = lastModifiedDate;
		this.deleted = false;
		this.calculateOnly = calculateOnly;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public SecUser getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(SecUser createdBy) {
		this.createdBy = createdBy;
	}

	public Date getCreatedDate() {
			return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public SecUser getLastModifiedBy() {
		return lastModifiedBy;
	}

	public void setLastModifiedBy(SecUser lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}

	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	//exclude from serialization
	@JsonIgnore
	public boolean isCalculateOnly() {
		return calculateOnly;
	}

	//include in deserialization
	@JsonProperty
	public void setCalculateOnly(boolean calculateOnly) {
		this.calculateOnly = calculateOnly;
	}
}
