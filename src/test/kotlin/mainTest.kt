import mapper.adaptTo
import org.junit.jupiter.api.Test

class FirstTest {
    data class Address(val id: String, val adr1: String, val ville: String, val codePostal: Int)
    data class AddressDTO(val adr1: String, val ville: String, val codePostal: Int)
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
    fun testAdapt() {
        val person = Person(
            "lorem@email.com", "person", "person", Address(
                "000", "new york", "new york", 2010
            )
        )

        ///-------------------------------------------------------------------
        val startTimeAdapter = System.currentTimeMillis()
        val dto = person.adaptTo(PersonDTO::class)
        val endTimeAdapter = System.currentTimeMillis()
        val timeExecutionAdapter = endTimeAdapter - startTimeAdapter
        ///-------------------------------------------------------------------

        val startTimeManual = System.currentTimeMillis()
        person.toDTO()
        val endTimeManual = System.currentTimeMillis()
        val timeExecutionManual = startTimeManual - endTimeManual

        ///-------------------------------------------------------------------
        println("adaptTo timer:${timeExecutionAdapter} ms ")
        println("manual Mapper timer:${timeExecutionManual} ms ")
        assert(
            dto == PersonDTO(
                "lorem@email.com", "person", AddressDTO(
                    "new york", "new york", 2010
                )
            )
        )
    }

}