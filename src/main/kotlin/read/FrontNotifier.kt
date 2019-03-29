package com.example.read

import com.example.read.pet.PetQueryObject
import com.example.server.SubscribeEvent
import com.example.subscribeRegistry
import com.example.write.pet.PetRegistered
import com.example.write.pet.PetVaccinated
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.AllowReplay
import org.axonframework.eventhandling.EventHandler

@ProcessingGroup("notifier.front")
//@AllowReplay(value = false)
class FrontNotifier {

    @EventHandler
    fun on(evt: PetRegistered) {
        subscribeRegistry.notify(SubscribeEvent.PET_REGISTERED, evt)
    }

    @EventHandler
    fun on(evt: PetVaccinated) {
        subscribeRegistry.notify(SubscribeEvent.PET_VACCINATED, evt)
    }
}