package bg.latona.santa.entities.santa.finance;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.santa.common.CCcGoodsType;
import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data //auto-create getters and setters
@Audited
@AllArgsConstructor
@NoArgsConstructor
@Entity //JPA persisted class
public class FCtGood extends CompanyRecord {

	private String code; //NOT NULL, -- code, out_code, gte_code - unique
	@ManyToOne
	private CCcOrganizationUnit outCode; //NOT NULL, -- organization unit
	@Column(length = 3000)
	private String descr; //NOT NULL,
	private String measure;
	private BigDecimal price;
	private BigDecimal priceAdditional;
	private String oldId;
	@ManyToOne
	private CCcGoodsType gteId; //NOT NULL,
	private LocalDate activeFromDate; //NOT NULL DEFAULT ('now'::text)::date,
	private LocalDate activeToDate; //NOT NULL DEFAULT '2100-01-01'::date,
	private String outMask;
}
/* COMMENT ON TABLE accounting.ct_goods
IS '! god !
goods and sevices';
COMMENT ON COLUMN accounting.ct_goods.code IS 'code, out_code, gte_code - unique';
COMMENT ON COLUMN accounting.ct_goods.out_code IS 'organization unit'; */