package bg.latona.santa.entities.selfie;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.DBFile;
import bg.latona.santa.entities.nepal.PowerPlant;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


@Data //auto-create getters and setters
@AllArgsConstructor
@NoArgsConstructor
@Audited
@ToString(exclude = {"sapXmls", "electricityInvoices"})
@EqualsAndHashCode(exclude = {"sapXmls", "electricityInvoices"})
@Builder
@Entity //JPA persisted class
public class ElectricityInvoice extends CompanyRecord {

//    @ValidateNotNull
    @Column
    private LocalDate taxEventDate; // Дата на данъчно събитие /dd.mm.yyyy/ change

    @Column
    private String reportingPointOwn; // ТО /C-33/

    @Column
    private LocalDate periodFrom; // Период от /dd.mm.yyyy/

    @Column
    private LocalDate periodTo; // Период до /dd.mm.yyyy/

    @Column
    private BigDecimal totalQuantity; // Общо количество edit param

    @Column
    private BigDecimal priceInLevs; // Цена в лв edit param

    @Column
    private BigDecimal priceInEuros; // Цена в евро

    @Column
    private BigDecimal vatInLevs;

    @Column
    private BigDecimal totalSumInLevs; // Обща сума в лв // без ддс

    @Column
    private BigDecimal vatInEuros;

    @Column
    private BigDecimal totalSumInEuros; // Обща сума в евро //без ддс

    @Column
    private String invoiceNumber; // Getting next number


    @Column(columnDefinition = "boolean default false")
    private Boolean sent; // Изпратен

    @Column(columnDefinition = "boolean default false")
    private Boolean isValid; // Are the two files merged or not

    @Column(columnDefinition = "boolean default false")
    private Boolean hasDbFile; // Is a DB created at this point or not


    @ManyToOne
    private AgreementType agreementType; // Кода на “Вид на услугата”

    @ManyToOne
    private LoiMeasurementUnit loiMeasurementUnit; // Мерна единица латински /MWh,kWh/

    @ManyToOne
    private LoiDocumentType loiDocumentType; // Вид на документа edit param debit or credit

    @ManyToOne
    private DBFile dbFile;

	@ManyToOne
	private PowerPlant powerPlant;

    @JsonIgnore
    @OneToMany(mappedBy = "electricityInvoice")
    private List<SapXml> sapXmls;

	@ManyToOne
	private ElectricityInvoice electricityInvoice;

	@JsonIgnore
	@OneToMany(mappedBy = "electricityInvoice")
	private List<ElectricityInvoice> electricityInvoices;


    @PrePersist
    public void prePersist() {
        if (this.hasDbFile == null) {
            this.hasDbFile = false;
        }
        this.sent = false;
    }
}
