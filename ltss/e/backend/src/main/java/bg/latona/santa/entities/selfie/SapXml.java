package bg.latona.santa.entities.selfie;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.DBFile;
import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Audited
@Entity
public class SapXml extends CompanyRecord {

    @ManyToOne
    private DBFile dbFile;

    @ManyToOne
    private ElectricityInvoice electricityInvoice;
}
