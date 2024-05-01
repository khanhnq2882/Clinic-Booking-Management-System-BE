package khanhnq.project.clinicbookingmanagementsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ClinicBookingManagementSystemApplication{
	public static void main(String[] args) {
		SpringApplication.run(ClinicBookingManagementSystemApplication.class, args);
	}

}
