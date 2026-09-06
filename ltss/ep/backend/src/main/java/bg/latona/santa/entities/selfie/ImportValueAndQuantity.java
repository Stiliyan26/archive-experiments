package bg.latona.santa.entities.selfie;

import bg.latona.santa.entities.CompanyRecord;
import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data //auto-create getters and setters
@AllArgsConstructor
@NoArgsConstructor
@Audited
@Entity //JPA persisted class
@Builder
public class ImportValueAndQuantity extends CompanyRecord {

    @Column
    private String reportingPointOwn; // ТО /C-33/

    @ManyToOne
    private LoiDocumentType loiDocumentType; // Вид на документа

    @ManyToOne
    private AgreementType agreementType; // Кода на “Вид на услугата”

    @ManyToOne
    private LoiMeasurementUnit loiMeasurementUnit; // Мерна единица латински /MWh,kWh/

    @Column
    private BigDecimal totalSumInLevs; // Обща сума в лв

    @Column
    private LocalDate periodFrom; // Период от /dd.mm.yyyy/

    @Column
    private LocalDate periodTo; // Период до /dd.mm.yyyy/

    @Column
    private BigDecimal totalQuantity; // Общо количество

    @Column
    private BigDecimal priceInLevs; // Цена в лв

    @Column
    private Boolean isValid; // Валидност на записа, default: true

    @OneToOne
    private ElectricityInvoice electricityInvoice;
}
