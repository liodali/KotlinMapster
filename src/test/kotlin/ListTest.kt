import io.github.serpro69.kfaker.Faker
import mapper.adaptListTo
import org.junit.jupiter.api.Test
import java.util.Random

class ListTest {
    data class Address(val id: Int, val adr1: String, val ville: String, val codePostal: String)
    data class AddressDTO(val adr1: String, val ville: String, val codePostal: String)
    data class Person(val email: String, val password: String, val firstName: String, val adr: Address)
    data class PersonDTO(val email: String, val firstName: String, val adr: AddressDTO)

    data class ClassRoom(val id: Int, val name: String, val etuds: List<Person>, val dateModification: String)
    data class ClassRoomDTO(val name: String, val etuds: List<PersonDTO>)

    private fun Address.toAdrDTO(): AddressDTO {
        return AddressDTO(
            this.adr1,
            this.ville,
            this.codePostal
        )
    }

    private fun Person.toDTO(): PersonDTO {
        return PersonDTO(
            this.email,
            this.firstName,
            this.adr.toAdrDTO()
        )
    }

    private fun ClassRoom.toDTO(): ClassRoomDTO {
        return ClassRoomDTO(
            this.name,
            this.etuds.map {
                it.toDTO()
            }
        )
    }

    @Test
    fun testAdaptListTo() {
        val faker = Faker()
        val persons = emptyList<Person>().toMutableList()
        val personDTOs = emptyList<PersonDTO>().toMutableList()
        for (i in 0..3) {
            val p = Person(
                "${faker.name.name()}@email.com",
                faker.backToTheFuture.quotes(),
                faker.name.firstName(),
                Address(
                    Random().nextInt(1000),
                    faker.address.streetAddress(),
                    faker.address.city(),
                    faker.address.postcode()
                )
            )
            persons.add(
                p
            )
            personDTOs.add(
                p.toDTO()
            )
        }

        // -------------------------------------------------------------------
        val dtos = persons.adaptListTo(PersonDTO::class)
        println(dtos)
        println("///-------------------------------------------------------------------\n")
        println(persons)

        // / -------------------------------------------------------------------
        assert(
            dtos.first() == personDTOs.first()
        )
    }
    @Test
    fun testNestedAdaptListTo() {
        val faker = Faker()
        val persons = emptyList<Person>().toMutableList()
        val personDTOs = emptyList<PersonDTO>().toMutableList()
        for (i in 0..1) {
            val p = Person(
                "${faker.name.name()}@email.com",
                faker.backToTheFuture.quotes(),
                faker.name.firstName(),
                Address(
                    Random().nextInt(1000),
                    faker.address.streetAddress(),
                    faker.address.city(),
                    faker.address.postcode()
                )
            )
            persons.add(
                p
            )
            personDTOs.add(
                p.toDTO()
            )
        }
        val classRooms = emptyList<ClassRoom>().toMutableList()
        for (i in 0..2) {
            classRooms.add(
                ClassRoom(
                    id = Random().nextInt(1000),
                    name = faker.company.name(),
                    etuds = persons,
                    dateModification = "12/12/2020"
                )
            )
        }
        val classRoomDTOs = classRooms.map {
            it.toDTO()
        }.toList()
        // / -------------------------------------------------------------------
        val dtos = classRooms.adaptListTo(ClassRoomDTO::class)
        println(dtos)
        println("///-------------------------------------------------------------------\n")
        println(classRoomDTOs)

        // / -------------------------------------------------------------------
        assert(
            dtos.first() == classRoomDTOs.first()
        )
    }
}
