package com.example.server

import com.example.gson
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentHashMap

class WsSubscribeRegistry {

    private val subscriptions = ConcurrentHashMap<SubscribeEvent, MutableSet<WebSocketSession>>()

    fun subscribe(event: SubscribeEvent, socketSession: WebSocketSession) {
        val sessions = subscriptions.computeIfAbsent(event) { HashSet<WebSocketSession>()}

        sessions.add(socketSession)

        println("subscribed $socketSession to $event")
    }

    fun unsubscribe(event: SubscribeEvent, socketSession: WebSocketSession) {
        val sessions = subscriptions.computeIfAbsent(event) { HashSet<WebSocketSession>()}

        sessions.remove(socketSession)

        println("unsubscribed $socketSession to $event")
    }

    fun unsubscribeSession(socketSession: WebSocketSession) {
        subscriptions.forEach {
            it.value.remove(socketSession)
        }
    }

    fun notify(event: SubscribeEvent, payload: Any) {
        println("notify about $event")
        subscriptions[event]?.forEach {
            GlobalScope.launch {
                it.send(
                    Frame.Text(
                        gson.toJson(eventMessage(event.key, payload))
                    )
                )
            }
        }
    }
}

enum class SubscribeEvent(val key: String) {
    PET_REGISTERED("pet.registered"),
    PET_VACCINATED("pet.vaccinated")
}

data class eventMessage(val eventKey: String, val payload: Any)

