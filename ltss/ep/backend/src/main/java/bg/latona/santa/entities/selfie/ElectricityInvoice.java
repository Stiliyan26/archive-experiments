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

    private LocalDate taxEventDate; // Дата на данъчно събитие /dd.mm.yyyy/ change

    private String reportingPointOwn; // ТО /C-33/

    private LocalDate periodFrom; // Период от /dd.mm.yyyy/

    private LocalDate periodTo; // Период до /dd.mm.yyyy/

    private BigDecimal totalQuantity; // Общо количество edit param

    private BigDecimal priceInLevs; // Цена в лв edit param

    private BigDecimal priceInEuros; // Цена в евро

    private BigDecimal vatInLevs;

    private BigDecimal totalSumInLevs; // Обща сума в лв // без ддс

    private BigDecimal vatInEuros;

    private BigDecimal totalSumInEuros; // Обща сума в евро //без ддс

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

	@ManyToOne
	private ElectricityInvoice electricityInvoice;

    @JsonIgnore
    @OneToMany(mappedBy = "electricityInvoice")
    private List<SapXml> sapXmls;

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
