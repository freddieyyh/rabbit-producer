package io.freddie.rabbitproducer

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RabbitProducerApplication

fun main(args: Array<String>) {
    runApplication<RabbitProducerApplication>(*args)
}
