package com.cp.csms

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CsmsApplication

fun main(args: Array<String>) {
	runApplication<CsmsApplication>(*args)
}
