import io.github.serpro69.kfaker.Faker
import mapper.BaseMapper
import mapper.UndefinedDestinationObject
import mapper.mapTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TestInverseBaseMapper {
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
        val orignalObject = mapper.adaptInverse(dto)
        assert(orignalObject == user)
    }

    @Test
    fun baseMapTest() {
        data class User(val name: String, val password: String)
        data class UserDTO(val fullName: String?, val password: String)

        val name = faker.name.firstName()
        val pwd = "1234"
        val user = User(name, password = pwd)

        val mapper = BaseMapper.from(user).to(UserDTO::class)
            .mapTo("name", "fullName")
        val dto = mapper.adapt()
        assert(dto == UserDTO(fullName = name, password = pwd))
        val orignalObject = mapper.adaptInverse(dto)
        assert(orignalObject == user)
    }
}