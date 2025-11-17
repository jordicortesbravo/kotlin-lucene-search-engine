package com.jcortes

import org.slf4j.LoggerFactory
import org.springframework.boot.Banner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class Application

fun main(args: Array<String>) {
    val log = LoggerFactory.getLogger(Application::class.java)
    runApplication<Application>(*args) {
        setBannerMode(Banner.Mode.OFF)
    }
    log.info("Application initialized")
}
