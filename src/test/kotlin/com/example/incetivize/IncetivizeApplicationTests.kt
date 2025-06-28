package com.example.incetivize

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@org.junit.jupiter.api.Disabled("Disabled: context loads requires external DB")
@SpringBootTest(properties=["spring.flyway.enabled=false","spring.datasource.url=jdbc:h2:mem:testdb"] )
class IncetivizeApplicationTests {

	@Test
	fun contextLoads() {
	}

}
