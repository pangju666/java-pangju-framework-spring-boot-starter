package io.github.pangju666.framework.boot.autoconfigure

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class Application {
	static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}