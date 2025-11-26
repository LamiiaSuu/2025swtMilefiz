package de.hs_rm.de.milefiz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MilefizApplication {

	public static void main(String[] args) {
		SpringApplication.run(MilefizApplication.class, args);
	}

}
