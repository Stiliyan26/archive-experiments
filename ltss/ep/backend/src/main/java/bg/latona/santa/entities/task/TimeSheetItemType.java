package bg.latona.santa.entities.task;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.Audited;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import java.util.Date;
import java.util.List;

@Data //auto-create getters and setters
@ToString(exclude = {"timeSheetItems"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"timeSheetItems"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"company_id", "code"})) //for all unique constraints should be put Drools UNIQUE rules too
public class TimeSheetItemType extends CompanyRecord {
	public static final Long TIME_SHEET_ITEM_TYPE_WORK_DAY = Long.valueOf(0);
	public static final Long TIME_SHEET_ITEM_TYPE_PAID_LEAVE = Long.valueOf(1);
	public static final Long TIME_SHEET_ITEM_TYPE_MATERNITY_LEAVE = Long.valueOf(2);
	public static final Long TIME_SHEET_ITEM_TYPE_SICK_LEAVE = Long.valueOf(3);
	public static final Long TIME_SHEET_ITEM_TYPE_COMPENSATION = Long.valueOf(4);
	public static final Long TIME_SHEET_ITEM_TYPE_NO_SHOW = Long.valueOf(5);
	public static final Long TIME_SHEET_ITEM_TYPE_NON_WORKING_DAYS = Long.valueOf(6);
	public static final Long TIME_SHEET_ITEM_TYPE_UNPAID_LEAVE = Long.valueOf(7);
	public static final Long TIME_SHEET_ITEM_TYPE_TRANSFERED_FROM_LAST_MONTH = Long.valueOf(8);
	public static final Long TIME_SHEET_ITEM_TYPE_OVERTIME = Long.valueOf(9);
	public static final Long TIME_SHEET_ITEM_TYPE_GOV_DUTY = Long.valueOf(10);

	private String name;
	private Long code;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "timeSheetItemType")
	private List<TimeSheetItem> timeSheetItems;

	public TimeSheetItemType() {};

	public TimeSheetItemType(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			String name, Long code) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.name = name;
		this.code = code;
	}
}
