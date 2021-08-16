import io.github.serpro69.kfaker.Faker
import kotlinx.coroutines.runBlocking
import mapper.BaseMapper
import mapper.ConfigMapper
import mapper.UndefinedDestinationObject
import mapper.ignore
import mapper.ignoreIf
import mapper.mapTo
import mapper.transformation
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.IllegalArgumentException
import java.security.MessageDigest

class TestBaseMapper {
    private val faker = Faker()

    @Test
    fun baseTest() {
        data class User(val name: String, val password: String)
        data class UserDTO(val name: String?, val password: String)

        val name = faker.name.firstName()
        val pwd = "1234"
        val user = User(name, password = pwd)

        val mapper = BaseMapper.from(user).to(UserDTO::class)
        val dto = mapper.adapt()
        assert(dto == UserDTO(name = name, password = pwd))
        /*
         * test exception,particular cases
         */

        /*
         * test case where destination object was not defined
         */
        val mapper2 = BaseMapper.from(user)
        assertThrows<UndefinedDestinationObject> {
            mapper2.adapt()
        }
    }

    @Test
    fun baseIgnoreIfTest() {
        data class User(val name: String, val password: String)
        data class UserDTO(val name: String?, val password: String?)

        val name = faker.name.firstName()

        val user = User(name, password = "1234")

        val mapper = BaseMapper.from(user).to(UserDTO::class)
            .ignoreIf("name") {
                it.name != name
            }.ignoreIf("password") {
                it.password != "password"
            }
            .ignoreIf("name") {
                it.name.isNotEmpty()
            }
        val dto = mapper.adapt()
        assert(dto == UserDTO(name, "1234"))

        val user1 = User(name, password = "1234")
        mapper.newConfig(ConfigMapper())
            .ignore("password")
        val dtoUser1 = mapper.adapt(user1)
        assert(dtoUser1 == UserDTO(name, null))
    }

    @Test
    fun baseTransformationTest() {
        data class User(val name: String, val password: String)
        data class UserDTO(val name: String?, val password: String)

        val name = faker.name.firstName()

        val user = User(name, password = "1234")
        val hashPwd = hashPassword(user.password)

        val mapper = BaseMapper.from(user).to(UserDTO::class)
            .transformation("password") {
                hashPassword(it.password)
            }
        val dto = mapper.adapt()
        assert(dto == UserDTO(name, hashPwd))
        var mapper2 = mapper.transformation("password") {
            "12345"
        }
        val dto2 = mapper2.adapt()
        assert(dto2.password == "12345")
        /*
         * test case where destination object was not defined
         */
        mapper2 = BaseMapper.from(user).to(UserDTO::class)
            .ignore("password").transformation("password") {
                hashPassword(it.password)
            }
        assertThrows<IllegalArgumentException> {
            mapper2.adapt()
        }
    }

    @Test
    fun baseTestMap() {
        data class User(val name: String, val email: String, val password: String, val country: String)
        data class LoginDTO(val login: String?, val password: String?)
        /*
         * data preparation
         */
        val name = faker.name.firstName()
        val email = faker.internet.email()
        val country = faker.address.country()
        val user = User(name, email, "1234", country)
        /*
         * BaseMapper builder with mapTo and transformation
         */
        val mapper = BaseMapper.from(user).to(LoginDTO::class)
            .mapTo("email", "login")
        /*
         * test exception
         */

        val dto = mapper.adapt()
        assert(dto == LoginDTO(email, "1234"))

        assertThrows<IllegalArgumentException> {
            mapper.mapTo("email", "password")
        }
    }

    @Test
    fun testNestedMap() {
        data class Address(val country: String, val fullAddress: String)
        data class AddressDTO(val country: String, val fullAddress: String)
        data class User(val name: String, val email: String, val adr: Address)
        data class UserDTO(val login: String?, val adr: AddressDTO)

        val name = faker.name.firstName()
        val email = faker.internet.email()
        val country = faker.address.country()
        val fullAdr = faker.address.fullAddress()
        val user = User(name, email, Address(country, fullAdr))
        val mapper = BaseMapper.from(user).to(UserDTO::class).mapTo("email", "login")
        val dto = mapper.adapt()

        assert(dto == UserDTO(email, AddressDTO(country, fullAdr)))
    }

    @Test
    fun testAsyncAdapt() {
        data class User(val name: String, val password: String)
        data class UserDTO(val name: String?, val password: String)

        val name = faker.name.firstName()
        val pwd = "1234"
        val user = User(name, password = pwd)

        val mapper = BaseMapper.from(user).to(UserDTO::class)
        val dto = runBlocking {
            mapper.adaptAsync()
        }
        assert(dto == UserDTO(name, pwd))
    }

    private fun hashPassword(password: String): String {
        val hex = "0123456789ABCDEF"
        val bytes = MessageDigest.getInstance("MD5").digest(password.toByteArray())
        val result = StringBuilder(bytes.size * 2)
        bytes.forEach {
            val i = it.toInt()
            result.append(hex[i shr 4 and 0x0f])
            result.append(hex[i and 0x0f])
        }
        return result.toString()
    }
}
