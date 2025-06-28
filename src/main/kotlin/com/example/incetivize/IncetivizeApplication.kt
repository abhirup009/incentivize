package com.example.incetivize

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.example"])
class IncetivizeApplication

fun main(args: Array<String>) {
	runApplication<IncetivizeApplication>(*args)
}
