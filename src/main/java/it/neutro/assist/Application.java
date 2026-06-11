package it.neutro.assist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EntityScan(basePackages = "it.neutro.assist.knowledge")
@EnableJpaRepositories(basePackages = "it.neutro.assist")
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
