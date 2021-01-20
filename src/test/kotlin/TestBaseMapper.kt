import io.github.serpro69.kfaker.Faker
import mapper.*
import org.junit.jupiter.api.Test
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
            }
        val dto = mapper.adapt()
        assert(dto == UserDTO(null, "1234"))

        val user1 = User(name, password = "1234")
        mapper.ignore("password")
        val dtoUser1 = mapper.adapt(user1)
        assert(dtoUser1 == UserDTO(name, null))
    }

    @Test
    fun baseTransformationTest() {
        data class User(val name: String, val team: String)
        data class UserDTO(val name: String?, val team: String?)

        val name = faker.name.firstName()
        val user = User(name, team = "FCBARCELONA")

        val mapper = BaseMapper.from(user).to(UserDTO::class)
            .transformation("team") {
                it.team.subSequence(0, 3)
            }
        val dto = mapper.adapt()
        assert(dto == UserDTO(name, "FCB"))
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
        val hashPwd = hashPassword(user.password)
        /*
         * BaseMapper builder with mapTo and transformation
         */
        val mapper = BaseMapper.from(user).to(LoginDTO::class)
            .mapTo("email", "login")
            .transformation("password") {
                hashPassword(it.password)
            }
        val dto = mapper.adapt()
        assert(dto == LoginDTO(email, hashPwd))
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
