package io.freddie.rabbitproducer.controller

import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@RestController
@RequestMapping("/api")
class HomeController(
    private val streamBridge: StreamBridge
) {
    val idGenerator = AtomicInteger(1)
    val map = ConcurrentHashMap<Int, Data>()

    /**
     * broadcast
     * 서버에 변경이 있는 경우 해당 이벤트를 모든 컴포넌트에 전파하고 싶을 경우
     */
    @PostMapping("/data")
    fun createData(@RequestBody data: Data): String {
        val id = idGenerator.getAndIncrement()
        map[id] = data
        sendCreateEvent(id, data)
        return "ID : $id"
    }


    @PutMapping("/data/{id}")
    fun updateOwner(@PathVariable id: Int, @RequestBody data: Data) : Data {
        map[id] = data
        if (data.owner != null) {
            sendUpdateOwnerEvent(id, data.owner)
        }

        if (data.data != null) {
            sendUpdateDataEvent(id, data.data)
        }

        return data
    }

    private fun sendCreateEvent(id: Int, data: Data) {
        val bindingName = "create-some-data"
        streamBridge.send(
            bindingName,
            mapOf(
                "id" to id,
                "data" to mapOf("owner" to data.owner, "data" to data.data),
                "time" to DateTimeFormatter.ISO_INSTANT.format(Instant.now())
            )
        )
    }

    private fun sendUpdateOwnerEvent(id: Int, owner: String) {
        val bindingName = "update-owner"
        streamBridge.send(
            bindingName,
            mapOf(
                "id" to id,
                "owner" to owner,
                "time" to DateTimeFormatter.ISO_INSTANT.format(Instant.now())
            )
        )
    }

    private fun sendUpdateDataEvent(id: Int, data: String) {
        val bindingName = if(data.contains("unicast")) "update-data-unicast" else "update-data"
        streamBridge.send(
            bindingName,
            mapOf(
                "id" to id,
                "data" to data,
                "time" to DateTimeFormatter.ISO_INSTANT.format(Instant.now())
            )
        )
    }

    data class Data(
        val owner: String? = null,
        val data: String? = null
    )
}