import io.github.serpro69.kfaker.Faker
import mapper.BaseMapper
import mapper.inverseTransformation
import mapper.mapMultiple
import mapper.mapTo
import mapper.transformation
import org.junit.jupiter.api.Test

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

    @Test
    fun baseInverseTransformationTest() {
        data class User(val firstName: String, val lastName: String, val password: String)
        data class UserDTO(val fullName: String, val password: String)

        val name = faker.name.firstName()
        val lastName = faker.name.lastName()
        val pwd = "1234"
        val user = User(name, lastName, password = pwd)

        val mapper = BaseMapper.from(user)
            .to(UserDTO::class).transformation(
                "fullName"
            ) { user ->
                user.firstName + " " + user.lastName
            }.inverseTransformation(
                "firstName"
            ) { dto ->
                dto.fullName.split(" ").first()
            }.inverseTransformation(
                "lastName"
            ) { dto ->
                dto.fullName.split(" ").last()
            }
            .mapMultiple(arrayOf("firstName", "lastName"), "fullName")
        val dto = mapper.adapt()
        assert(dto == UserDTO(fullName = "$name $lastName", password = pwd))
        val orignalObject = mapper.adaptInverse(dto)
        assert(orignalObject == user)
    }
}
