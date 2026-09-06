package bg.latona.santa.entities.santa.common;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.Audited;

import bg.latona.santa.entities.ListOptionItem;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;

@Data //auto-create getters and setters
@ToString(exclude = {"cCcGoodsTypes","cGoods"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"cCcGoodsTypes","cGoods"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class LoiGoodsType extends ListOptionItem {

	public static final Long GOODS_TYPE_GD = 1L;
	public static final Long GOODS_TYPE_BD = 2L;
	public static final Long GOODS_TYPE_SM = 3L;
	public static final Long GOODS_TYPE_RM = 4L;
	public static final Long GOODS_TYPE_VR = 5L;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "goodType")
	private List<CCcGoodsType> cCcGoodsTypes;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "goodType")
	private List<CGoods> cGoods;

	public LoiGoodsType() {
		super();
	}

	public LoiGoodsType(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			String listOptionItemName, Long listOptionItemCode) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, listOptionItemName, listOptionItemCode);
	}
}
