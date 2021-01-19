import io.github.serpro69.kfaker.Faker
import mapper.BaseMapper
import mapper.ignore
import mapper.ignoreIf
import mapper.transformation
import org.junit.jupiter.api.Test

class TestBaseMapper {
    val faker = Faker()

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
}
