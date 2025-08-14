package kz.nsanzhar.DailyBrowse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DailyBrowseApplication {

	public static void main(String[] args) {
		SpringApplication.run(DailyBrowseApplication.class, args);
	}

}