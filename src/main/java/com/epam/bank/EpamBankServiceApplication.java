package com.epam.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EpamBankServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(EpamBankServiceApplication.class, args);
	}

}
// TODO validation, also in front
// todo custom exceptions, handling
// todo handle if some services or db is not available