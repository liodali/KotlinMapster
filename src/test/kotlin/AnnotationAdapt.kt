import mapper.adaptTo
import mapper.annotations.MapTo
import org.junit.jupiter.api.Test

class AnnotationAdapt {

    data class Address(val id: String, val adr1: String, val ville: String, val codePostal: Int)
    data class AddressDTO(val adr1: String, val ville: String, val codePostal: Int)
    data class Person(@MapTo("login") val email: String, val password: String, val firstName: String, val adr: Address)
    data class PersonDTO(val email: String, val firstName: String, val adr: AddressDTO)

    data class LoginUser(val login: String, val password: String)

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

    private fun Person.toLogin(): LoginUser {
        return LoginUser(
            this.email,
            this.password
        )
    }

    @Test
    fun testMapToAnnotation() {
        val person = Person(
            "lorem@email.com", "person", "person", Address(
                "000", "new york", "new york", 2010
            )
        )
        val manualLogin = person.toLogin()
        val login = person.adaptTo(LoginUser::class)

        assert(manualLogin == login)
    }
}
