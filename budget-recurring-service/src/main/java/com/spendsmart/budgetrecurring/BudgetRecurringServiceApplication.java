package com.spendsmart.budgetrecurring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BudgetRecurringServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BudgetRecurringServiceApplication.class, args);
    }
}