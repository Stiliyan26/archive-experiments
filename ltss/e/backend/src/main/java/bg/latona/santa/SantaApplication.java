package bg.latona.santa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//following this guide: https://spring.io/guides/tutorials/react-and-spring-data-rest/
@SpringBootApplication
public class SantaApplication {

	public static void main(String[] args) {
		SpringApplication.run(SantaApplication.class, args);
	}
}
