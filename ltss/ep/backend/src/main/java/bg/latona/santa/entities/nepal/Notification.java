package bg.latona.santa.entities.nepal;

import bg.latona.santa.entities.CompanyRecord;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.time.LocalDateTime;

@Data //auto-create getters and setters
@AllArgsConstructor
@NoArgsConstructor
@Entity //JPA persisted class
public class Notification extends CompanyRecord {

	@Column(length = 3000)
	private String message;
	private LocalDateTime messageDateTime;
	private LocalDateTime expirationDate;
	private Boolean isActive;
	private String userName;
	private String roleName;
	private String identificationFirst;
	private String identificationSecond;
	private String identificationThird;
	private String identificationFourth;
	@ManyToOne
	private LoiNotificationType loiNotificationType;
}
