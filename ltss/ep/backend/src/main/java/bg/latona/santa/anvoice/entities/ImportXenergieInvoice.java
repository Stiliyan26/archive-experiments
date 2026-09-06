package bg.latona.santa.anvoice.entities;

import bg.latona.santa.entities.CompanyRecord;
import lombok.*;

import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

import javax.persistence.ManyToOne;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.person.LegalPerson;
import bg.latona.santa.entities.security.SecUser;
import bg.latona.santa.entities.selfie.ElectricityInvoice;

@Data // auto-create getters and setters
@ToString(exclude = {"electricityInvoice"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"electricityInvoice"}) //avoid recursion by Lombok
@Audited
@Entity // JPA persisted class
public class ImportXenergieInvoice extends CompanyRecord {

    @Column
    private LocalDate billingDate; // Отчетен месец
    @Column
    private LocalDate billingMonth; // Отчетен месец
    @Column
    private String contract; // Номер/ дата договор
    @Column
    private String znumber; // Z номер
    @Column
    private String customerEik; // ЕИК / ЕГН
    @Column
    private String customerName; // Име на дружество/ФЛ
    @Column
    private String address; // Седалище/адрес
    @Column
    private String vatNumber; // Ид.№ по ДДС
    @Column
    private BigDecimal quantity; // Количество, КВтч
    @Column
    private BigDecimal price; // Цена
    @Column
    private BigDecimal totalCost; // Стойност без ДДС
    @Column
    private BigDecimal totalCostInclVat; // Стойност с ДДС
    @Column
    private BigDecimal vat; // ДДС, лв.
    @Column
    private String reasonNoVat; // Основание за неначисляване на ДДС
    @Column
    private String email; // Имейл
    @Column
    private String product; // Вид услуга
    @Column
    private String selfBilling;
    @Column
    private String bankConnection;
    @Column
    private Long documentTypeId;
    @Column
    private String dealId;
    @Column
    private String reasonCorrectionDoc;
    @Column
    private String eprsEpres;
    @Column
    private BigDecimal eprsEpresEik;
    @Column
    private String eprsEpresAddress;

    @Column
    private Boolean isValid; // Валидност на записа, default: true

    @ManyToOne
    private LegalPerson legalPerson;

	@JsonIgnore //avoid serialization recursion by Jackson
    @OneToOne
    private ElectricityInvoice electricityInvoice;

    // default empty constructor
    public ImportXenergieInvoice() {}

    public ImportXenergieInvoice(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
            LocalDate billingDate, LocalDate billingMonth, String contract, String znumber, String customerEik, String customerName, 
            String address, String vatNumber, BigDecimal quantity, BigDecimal price, BigDecimal totalCost, BigDecimal totalCostInclVat, 
            BigDecimal vat, String reasonNoVat, String email, String product, String selfBilling, String bankConnection, 
            Long documentTypeId, String dealId, String reasonCorrectionDoc, String eprsEpres, BigDecimal eprsEpresEik, 
            String eprsEpresAddress, Boolean isValid,
            LegalPerson legalPerson, ElectricityInvoice electricityInvoice) {
        super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
        this.address = address;
        this.billingDate = billingDate;
        this.billingMonth = billingMonth;
        this.contract = contract;
        this.customerEik = customerEik;
        this.customerName = customerName;
        this.electricityInvoice = electricityInvoice;
        this.email = email;
        this.price = price;
        this.product = product;
        this.quantity = quantity;
        this.reasonNoVat = reasonNoVat;
        this.totalCost = totalCost;
        this.totalCostInclVat = totalCostInclVat;
        this.vat = vat;
        this.vatNumber = vatNumber;
        this.znumber = znumber;
        this.selfBilling = selfBilling;
        this.bankConnection = bankConnection;
        this.documentTypeId = documentTypeId;
        this.dealId = dealId;
        this.reasonCorrectionDoc = reasonCorrectionDoc;
        this.eprsEpres = eprsEpres;
        this.eprsEpresEik = eprsEpresEik;
        this.eprsEpresAddress = eprsEpresAddress;
        this.isValid = isValid;
        this.legalPerson = legalPerson;
    }

}
