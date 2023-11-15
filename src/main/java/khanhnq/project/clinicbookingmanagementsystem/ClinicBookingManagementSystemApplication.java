package khanhnq.project.clinicbookingmanagementsystem;

import khanhnq.project.clinicbookingmanagementsystem.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class ClinicBookingManagementSystemApplication implements CommandLineRunner {

	private final FileService fileService;

	public ClinicBookingManagementSystemApplication(FileService fileService) {
		this.fileService = fileService;
	}

	public static void main(String[] args) {
		SpringApplication.run(ClinicBookingManagementSystemApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		fileService.init();
	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**")
						.allowedOrigins("http://localhost:4200") // Replace with your Angular app URL
						.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Allowed HTTP methods
						.allowedHeaders("*") // Allowed request headers (you can customize this based on your requirements)
						.allowCredentials(true);
			}
		};
	}

}
