package com.example.read.pet

import com.example.write.pet.domain.Species
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object PetsSimple : Table("read_pet_simple") {
    val petId = varchar("pet_id", 44).primaryKey()
    val species = enumerationByName("species", 63, Species::class)
    val name = varchar("name", 255)
}

object PetQueryObjectRepository {
    init {
        Database.connect("jdbc:postgresql://localhost:15432/nursery", "org.postgresql.Driver", "root", "root")
    }

    fun add(pet: PetQueryObject) {
        transaction {
            PetsSimple.insert {
                it[petId] = pet.id
                it[species] = pet.species
                it[name] = pet.name
            }
        }
    }

    fun all(): List<PetQueryObject> {
        return transaction {
            PetsSimple.selectAll().map {
                PetQueryObject(
                    it[PetsSimple.petId],
                    it[PetsSimple.species],
                    it[PetsSimple.name]
                )
            }
        }
    }

    fun find(id: String): PetQueryObject? {
        return transaction {
            PetsSimple.select { PetsSimple.petId eq id }.firstOrNull()?.let {
                PetQueryObject(
                    it[PetsSimple.petId].toString(),
                    it[PetsSimple.species],
                    it[PetsSimple.name]
                )
            }
        }
    }
}