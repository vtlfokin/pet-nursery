package com.example

import org.axonframework.common.transaction.NoTransactionManager
import org.axonframework.config.Configuration
import org.axonframework.config.Configurer
import org.axonframework.config.DefaultConfigurer
import org.axonframework.eventhandling.tokenstore.TokenStore
import org.axonframework.eventhandling.tokenstore.jdbc.JdbcTokenStore
import org.axonframework.eventhandling.tokenstore.jdbc.PostgresTokenTableFactory
import org.axonframework.eventhandling.tokenstore.jdbc.TokenSchema
import org.axonframework.eventsourcing.eventstore.jdbc.JdbcEventStorageEngine
import org.axonframework.eventsourcing.eventstore.jdbc.PostgresEventTableFactory
import org.axonframework.serialization.xml.XStreamSerializer
import pet.domain.Pet
import java.sql.DriverManager

fun buildDefaultConfiguration(): Configuration = DefaultConfigurer.defaultConfiguration().apply {
    setUpEventStore()
    registerAggregates()
    registerEventProcessing()
}.buildConfiguration()


fun Configurer.setUpEventStore() {
    configureEmbeddedEventStore {
        val engine = JdbcEventStorageEngine.builder()
            .connectionProvider {
                DriverManager.getConnection("jdbc:postgresql://localhost:15432/nursery", "root", "root")
            }
            .transactionManager(NoTransactionManager.instance())

            .build()

        engine.createSchema(PostgresEventTableFactory.INSTANCE)

        engine
    }
}

fun Configurer.registerAggregates() {
    configureAggregate(Pet::class.java)
}

fun Configurer.registerEventProcessing() {
    registerComponent(TokenStore::class.java) { conf ->
        val store = JdbcTokenStore.builder()
            .schema(TokenSchema())
            .connectionProvider {
                DriverManager.getConnection("jdbc:postgresql://localhost:15432/nursery", "root", "root")
            }
            .serializer(XStreamSerializer.builder().build())
            .build()
        store.createSchema(PostgresTokenTableFactory())

        store
    }
    eventProcessing {
        // Регистрация слушателей событий (для обновления read модели)
        it.registerEventHandler{ PetQueryObjectUpdater() }
        it.registerEventHandler{ VaccinationQueryObjectUpdater() }
    }
}