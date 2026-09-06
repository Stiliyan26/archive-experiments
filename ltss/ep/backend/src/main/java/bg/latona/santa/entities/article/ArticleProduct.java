package bg.latona.santa.entities.article;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

import bg.latona.santa.entities.person.ClientInterest;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.Audited;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;

@Data //auto-create getters and setters
@ToString(exclude = {"clientInterests"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"clientInterests"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class ArticleProduct extends Article {
	
	private String predNom;
	private String nomNom;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "articleProduct")
	private List<ClientInterest> clientInterests;
	
	public ArticleProduct() {};
	
	public ArticleProduct(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company, String name, String foreignId,
			String predNom, String nomNom, String measureForeignId, String measure, String measureShort) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, name, foreignId, measureForeignId, measure, measureShort);
		this.predNom = predNom;
		this.nomNom = nomNom;
	}
}
