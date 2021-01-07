import io.github.serpro69.kfaker.Faker
import mapper.adaptListTo
import org.junit.jupiter.api.Test
import java.util.*

class TestList {
    data class Address(val id: Int, val adr1: String, val ville: String, val codePostal: String)
    data class AddressDTO(val adr1: String, val ville: String, val codePostal: String)
    data class Person(val email: String, val password: String, val firstName: String, val adr: Address)
    data class PersonDTO(val email: String, val firstName: String, val adr: AddressDTO)

    private fun Person.toDTO(): PersonDTO {
        return PersonDTO(
            this.email,
            this.firstName,
            AddressDTO(
                this.adr.adr1,
                this.adr.ville,
                this.adr.codePostal
            )
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

        ///-------------------------------------------------------------------
        val dtos = persons.adaptListTo(PersonDTO::class)
        println(dtos)
        println("///-------------------------------------------------------------------\n")
        println(persons)

        ///-------------------------------------------------------------------
        assert(
            dtos.first() == personDTOs.first()
        )
    }

}