package bg.latona.santa.entities.santa.common;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.*;

import bg.latona.santa.entities.santa.finance.FDiscount;
import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonIgnore;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

//https://thorben-janssen.com/result-set-mapping-complex-mappings/
@SqlResultSetMapping(
		name = "getOrderGoodsMapping",
		entities = {
			@EntityResult(entityClass = CGoods.class,fields = {
					@FieldResult(name = "id", column = "cgoods_id"),
					@FieldResult(name = "createdDate", column = "cgoods_created_date"),
					@FieldResult(name = "deleted", column = "cgoods_deleted"),
					@FieldResult(name = "lastModifiedDate", column = "cgoods_last_modified_date"),
					@FieldResult(name = "barcode", column = "cgoods_barcode"),
					@FieldResult(name = "bundleQuantity", column = "cgoods_bundle_quantity"),
					@FieldResult(name = "code", column = "cgoods_code"),
					@FieldResult(name = "description", column = "cgoods_description"),
					@FieldResult(name = "manufacturerCode", column = "cgoods_manufacturer_code"),
					@FieldResult(name = "maxQuantity", column = "cgoods_max_quantity"),
					@FieldResult(name = "minQuantity", column = "cgoods_min_quantity"),
					@FieldResult(name = "nameBg", column = "cgoods_name_bg"),
					@FieldResult(name = "nameEng", column = "cgoods_name_eng"),
					@FieldResult(name = "volume", column = "cgoods_volume"),
					@FieldResult(name = "weight", column = "cgoods_weight"),
					@FieldResult(name = "createdBy", column = "cgoods_created_by_id"),
					@FieldResult(name = "lastModifiedBy", column = "cgoods_last_modified_by_id"),
					@FieldResult(name = "company", column = "cgoods_company_id"),
					@FieldResult(name = "defaultVendor", column = "cgoods_default_vendor_id"),
					@FieldResult(name = "godId", column = "cgoods_god_id_id"),
					@FieldResult(name = "goodMark", column = "cgoods_good_mark_id"),
					@FieldResult(name = "goodType", column = "cgoods_good_type_id"),
					@FieldResult(name = "gteId", column = "cgoods_gte_id_id"),
					@FieldResult(name = "meeId", column = "cgoods_mee_id_id"),
					@FieldResult(name = "outCode", column = "cgoods_out_code_id")
			}),
			@EntityResult(entityClass = CCcPartner.class,fields = {
					@FieldResult(name = "id", column = "ccc_partner_id"),
					@FieldResult(name = "createdDate", column = "ccc_partner_created_date"),
					@FieldResult(name = "deleted", column = "ccc_partner_deleted"),
					@FieldResult(name = "lastModifiedDate", column = "ccc_partner_last_modified_date"),
					@FieldResult(name = "activeFrom", column = "ccc_partner_active_from"),
					@FieldResult(name = "activeTo", column = "ccc_partner_active_to"),
					@FieldResult(name = "address", column = "ccc_partner_address"),
					@FieldResult(name = "bulstat", column = "ccc_partner_bulstat"),
					@FieldResult(name = "code", column = "ccc_partner_code"),
					@FieldResult(name = "egn", column = "ccc_partner_egn"),
					@FieldResult(name = "email", column = "ccc_partner_email"),
					@FieldResult(name = "fax", column = "ccc_partner_fax"),
					@FieldResult(name = "foreignNo", column = "ccc_partner_foreign_no"),
					@FieldResult(name = "mol", column = "ccc_partner_mol"),
					@FieldResult(name = "name", column = "ccc_partner_name"),
					@FieldResult(name = "oldCode", column = "ccc_partner_old_code"),
					@FieldResult(name = "tel", column = "ccc_partner_tel"),
					@FieldResult(name = "vatNo", column = "ccc_partner_vat_no"),
					@FieldResult(name = "createdBy", column = "ccc_partner_created_by_id"),
					@FieldResult(name = "lastModifiedBy", column = "ccc_partner_last_modified_by_id"),
					@FieldResult(name = "company", column = "ccc_partner_company_id"),
					@FieldResult(name = "accountMgrUser", column = "ccc_partner_account_mgr_user_id"),
					@FieldResult(name = "legalStatus", column = "ccc_partner_legal_status_id"),
					@FieldResult(name = "outCode", column = "ccc_partner_out_code_id"),
					@FieldResult(name = "partnerGroup", column = "ccc_partner_partner_group_id"),
					@FieldResult(name = "partnerType", column = "ccc_partner_partner_type_id")
			}),
			@EntityResult(entityClass = CMeasure.class,fields = {
					@FieldResult(name = "id", column = "cmeasure_id"),
					@FieldResult(name = "createdDate", column = "cmeasure_created_date"),
					@FieldResult(name = "deleted", column = "cmeasure_deleted"),
					@FieldResult(name = "lastModifiedDate", column = "cmeasure_last_modified_date"),
					@FieldResult(name = "code", column = "cmeasure_code"),
					@FieldResult(name = "cofficient", column = "cmeasure_cofficient"),
					@FieldResult(name = "name", column = "cmeasure_name"),
					@FieldResult(name = "createdBy", column = "cmeasure_created_by_id"),
					@FieldResult(name = "lastModifiedBy", column = "cmeasure_last_modified_by_id"),
					@FieldResult(name = "company", column = "cmeasure_company_id"),
					@FieldResult(name = "meeId", column = "cmeasure_mee_id_id")
			}),
			@EntityResult(entityClass = COrder.class,fields = {
					@FieldResult(name = "id", column = "corder_id"),
					@FieldResult(name = "createdDate", column = "corder_created_date"),
					@FieldResult(name = "deleted", column = "corder_deleted"),
					@FieldResult(name = "lastModifiedDate", column = "corder_last_modified_date"),
					@FieldResult(name = "dateOrr", column = "corder_date_orr"),
					@FieldResult(name = "finishDate", column = "corder_finish_date"),
					@FieldResult(name = "orderNum", column = "corder_order_num"),
					@FieldResult(name = "timeLimit", column = "corder_time_limit"),
					@FieldResult(name = "total", column = "corder_total"),
					@FieldResult(name = "createdBy", column = "corder_created_by_id"),
					@FieldResult(name = "lastModifiedBy", column = "corder_last_modified_by_id"),
					@FieldResult(name = "company", column = "corder_company_id"),
					@FieldResult(name = "currency", column = "corder_currency_id"),
					@FieldResult(name = "outCode", column = "corder_out_code_id"),
					@FieldResult(name = "parId", column = "corder_par_id_id"),
					@FieldResult(name = "status", column = "corder_status_id")
			}),
			@EntityResult(entityClass = COrderDetail.class,fields = {
					@FieldResult(name = "id", column = "corder_detail_id"),
					@FieldResult(name = "createdDate", column = "corder_detail_created_date"),
					@FieldResult(name = "deleted", column = "corder_detail_deleted"),
					@FieldResult(name = "lastModifiedDate", column = "corder_detail_last_modified_date"),
					@FieldResult(name = "cancelledQuantity", column = "corder_detail_cancelled_quantity"),
					@FieldResult(name = "ddlQuantity", column = "corder_detail_ddl_quantity"),
					@FieldResult(name = "plnQuantity", column = "corder_detail_pln_quantity"),
					@FieldResult(name = "price", column = "corder_detail_price"),
					@FieldResult(name = "priceConfirm", column = "corder_detail_price_confirm"),
					@FieldResult(name = "quantity", column = "corder_detail_quantity"),
					@FieldResult(name = "quantityConfirm", column = "corder_detail_quantity_confirm"),
					@FieldResult(name = "rqyQuantity", column = "corder_detail_rqy_quantity"),
					@FieldResult(name = "stkQuantity", column = "corder_detail_stk_quantity"),
					@FieldResult(name = "createdBy", column = "corder_detail_created_by_id"),
					@FieldResult(name = "lastModifiedBy", column = "corder_detail_last_modified_by_id"),
					@FieldResult(name = "company", column = "corder_detail_company_id"),
					@FieldResult(name = "doiId", column = "corder_detail_doi_id_id"),
					@FieldResult(name = "godId", column = "corder_detail_god_id_id"),
					@FieldResult(name = "meeId", column = "corder_detail_mee_id_id"),
					@FieldResult(name = "orrId", column = "corder_detail_orr_id_id")
			}),
			@EntityResult(entityClass = CPriceList.class,fields = {
					@FieldResult(name = "id", column = "cprice_list_id"),
					@FieldResult(name = "createdDate", column = "cprice_list_created_date"),
					@FieldResult(name = "deleted", column = "cprice_list_deleted"),
					@FieldResult(name = "lastModifiedDate", column = "cprice_list_last_modified_date"),
					@FieldResult(name = "basePrice", column = "cprice_list_base_price"),
					@FieldResult(name = "discount", column = "cprice_list_discount"),
					@FieldResult(name = "endDate", column = "cprice_list_end_date"),
					@FieldResult(name = "price1", column = "cprice_list_price1"),
					@FieldResult(name = "price2", column = "cprice_list_price2"),
					@FieldResult(name = "price3", column = "cprice_list_price3"),
					@FieldResult(name = "price4", column = "cprice_list_price4"),
					@FieldResult(name = "price5", column = "cprice_list_price5"),
					@FieldResult(name = "startDate", column = "cprice_list_start_date"),
					@FieldResult(name = "status", column = "cprice_list_status"),
					@FieldResult(name = "createdBy", column = "cprice_list_created_by_id"),
					@FieldResult(name = "lastModifiedBy", column = "cprice_list_last_modified_by_id"),
					@FieldResult(name = "company", column = "cprice_list_company_id"),
					@FieldResult(name = "currency", column = "cprice_list_currency_id"),
					@FieldResult(name = "doiId", column = "cprice_list_doi_id_id"),
					@FieldResult(name = "godId", column = "cprice_list_god_id_id"),
					@FieldResult(name = "meeId", column = "cprice_list_mee_id_id"),
					@FieldResult(name = "outCode", column = "cprice_list_out_code_id"),
					@FieldResult(name = "parId", column = "cprice_list_par_id_id"),
					@FieldResult(name = "stkId", column = "cprice_list_stk_id_id")
			})
		},
		columns = {
			@ColumnResult(name = "mee_cofficient", type = BigDecimal.class),
			@ColumnResult(name = "rqy_quantity", type = BigDecimal.class),
			@ColumnResult(name = "stk_quantity", type = BigDecimal.class),
			@ColumnResult(name = "quantity_confirm", type = BigDecimal.class),
			@ColumnResult(name = "quantity_not_confirmed", type = BigDecimal.class),
			@ColumnResult(name = "min_price", type = BigDecimal.class)
		}
)
@Data //auto-create getters and setters
@ToString(exclude = {"cDeliveryGoodMaps","reserveQuantities","cDeliveryDetails","cDeliveryOfferItems","cOfferDetails","cStocks",
"cOrderDetails","cPriceLists","cRequestDetails","fDiscounts","cInvoiceRows"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"cDeliveryGoodMaps","reserveQuantities","cDeliveryDetails","cDeliveryOfferItems","cOfferDetails","cStocks",
"cOrderDetails","cPriceLists","cRequestDetails","fDiscounts","cInvoiceRows"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
@Table(indexes = {
		@Index(name = "ix_CGoods_gteId", columnList = "gte_id_id"),
		@Index(name = "ix_CGoods_goodType", columnList = "good_type_id"),
		@Index(name = "ix_CGoods_outCode", columnList = "out_code_id"),
		@Index(name = "ix_CGoods_meeId", columnList = "mee_id_id"),
		@Index(name = "ix_CGoods_godId", columnList = "god_id_id"),
		@Index(name = "ix_CGoods_goodMark", columnList = "good_mark_id"),
		@Index(name = "ix_CGoods_defaultVendor", columnList = "default_vendor_id")
	})
public class CGoods extends CompanyRecord{
	private String nameBg;
	private String nameEng;
	private String code;
	private String barcode;
	private String manufacturerCode;
	@ManyToOne
	private CCcGoodsType gteId;
	@ManyToOne
	private LoiGoodsType goodType; //CONSTRAINT god_good_type_chk CHECK (((good_type)::text = ANY (ARRAY[('GD'::character varying)::text, ('RM'::character varying)::text, ('BD'::character varying)::text, ('SM'::character varying)::text])))
	@ManyToOne 
	private CCcOrganizationUnit outCode;
	private Integer minQuantity;
	private Integer maxQuantity;
	@ManyToOne
	private CMeasure meeId;
	private String description;
	private BigDecimal weight;
	private BigDecimal volume;
	private BigDecimal bundleQuantity;
	//private String outMask; parent id of out_code 
	@ManyToOne
	private CGoods godId;
	@ManyToOne 
	private CGoodMark goodMark;
	@ManyToOne 
	private CCcPartner defaultVendor;
	
	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "godId")
	private List<CDeliveryGoodMap> cDeliveryGoodMaps;
	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "godId")
	private List<CReserveQuantity> reserveQuantities;
	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "godId")
	private List<CDeliveryDetail> cDeliveryDetails;
	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "godId")
	private List<CDeliveryOfferItem> cDeliveryOfferItems;
	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "godId")
	private List<COfferDetail> cOfferDetails;
	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "godId")
	private List<COrderDetail> cOrderDetails;
	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "godId")
	private List<CPriceList> cPriceLists;
	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "godId")
	private List<CRequestDetail> cRequestDetails;
	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "godId")
	private List<CStock> cStocks;
	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "god")
	private List<FDiscount> fDiscounts;
	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "article")
	private List<CInvoiceRow> cInvoiceRows;

	
	public CGoods() {}

	public CGoods(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company, 
			String nameBg, String nameEng, String code, String barcode, String manufacturerCode, CCcGoodsType gteId, LoiGoodsType goodType,
			CCcOrganizationUnit outCode, Integer minQuantity, Integer maxQuantity, CMeasure meeId, String description,
			BigDecimal weight, BigDecimal volume, BigDecimal bundleQuantity, CGoodMark goodMark, CGoods godId, CCcPartner defaultVendor) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.nameBg = nameBg;
		this.nameEng = nameEng;
		this.code = code;
		this.barcode = barcode;
		this.manufacturerCode = manufacturerCode;
		this.gteId = gteId;
		this.goodType = goodType;
		this.outCode = outCode;
		this.minQuantity = minQuantity;
		this.maxQuantity = maxQuantity;
		this.meeId = meeId;
		this.description = description;
		this.weight = weight;
		this.volume = volume;
		this.bundleQuantity = bundleQuantity;
		this.goodMark = goodMark;
		this.godId = godId;
		this.defaultVendor = defaultVendor;
	}
}
