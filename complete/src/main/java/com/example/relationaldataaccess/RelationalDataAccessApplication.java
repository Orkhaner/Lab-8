package com.example.relationaldataaccess;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
public class RelationalDataAccessApplication implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(RelationalDataAccessApplication.class);

	public static void main(String args[]) {
		SpringApplication.run(RelationalDataAccessApplication.class, args);
	}

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Override
	public void run(String... strings) throws Exception {

		log.info("Creating tables if not exist");

		// Ensure the table exists but do not delete data
		jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS customers(" +
				"id SERIAL, first_name VARCHAR(255), last_name VARCHAR(255))");

		List<Object[]> splitUpNames = Arrays.asList("John Woo", "Jeff Dean", "Josh Bloch", "Josh Long").stream()
				.map(name -> name.split(" "))
				.collect(Collectors.toList());

		splitUpNames.forEach(name -> log.info(String.format("Inserting customer record for %s %s", name[0], name[1])));

		// Insert new data (2 new records)
		Object[] newRecord1 = new Object[]{"Orkhan", "65644"};
		Object[] newRecord2 = new Object[]{"Orkhan", "Rahimli"};
		jdbcTemplate.update("INSERT INTO customers(first_name, last_name) VALUES (?, ?)", newRecord1);
		jdbcTemplate.update("INSERT INTO customers(first_name, last_name) VALUES (?, ?)", newRecord2);

		// Query and log new records
		log.info("Querying for newly added customer records by first_name:");
		List<Customer> newCustomers = jdbcTemplate.query(
				"SELECT id, first_name, last_name FROM customers WHERE first_name IN (?, ?)",
				(rs, rowNum) -> new Customer(rs.getLong("id"), rs.getString("first_name"), rs.getString("last_name")),
				"Alice", "Orkhan");
		newCustomers.forEach(customer -> log.info(customer.toString()));
	}

	// Add a REST controller to expose querying functionality
	@RestController
	class CustomerController {

		@Autowired
		JdbcTemplate jdbcTemplate;

		@GetMapping("/customers")
		public List<Customer> getCustomersByName(@RequestParam String firstName) {
			return jdbcTemplate.query(
					"SELECT id, first_name, last_name FROM customers WHERE first_name = ?",
					(rs, rowNum) -> new Customer(rs.getLong("id"), rs.getString("first_name"), rs.getString("last_name")),
					firstName);
		}
	}
}
