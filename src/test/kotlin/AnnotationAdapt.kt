import io.github.serpro69.kfaker.Faker
import mapper.adaptTo
import mapper.annotations.CombineTo
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

    @Test
    fun testCombineToAnnotation() {
        val faker = Faker()

        data class User(
            @CombineTo(destAtt = "fullName", index = 0) val firstName: String,
            @CombineTo(destAtt = "fullName", index = 1) val lastName: String,
            val CIN: String
        )

        data class UserDTO(
            val fullName: String,
            val CIN: String
        )

        fun User.toDTO(): UserDTO {
            return UserDTO(
                fullName = "${this.firstName} ${this.lastName}",
                CIN = this.CIN
            )
        }

        val user = User(
            firstName = faker.name.firstName(),
            lastName = faker.name.lastName(),
            CIN = "0000"
        )
        val manuallyDTO = user.toDTO()
        val dyo = user.adaptTo(UserDTO::class)

        assert(manuallyDTO == dyo)
    }
}
