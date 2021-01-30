import io.github.serpro69.kfaker.Faker
import mapper.BaseMapper
import mapper.UndefinedSourceObject
import mapper.adaptListTo
import mapper.ignore
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TestListBaseMapper {
    private val faker = Faker()

    @Test
    fun testBaseList() {
        data class User(val name: String, val password: String)
        data class UserDTO(val name: String?, val password: String?)

        val users = emptyList<User>().toMutableList()
        for (i in 0..2) {
            val name = faker.name.firstName()
            val pwd = "1234"
            users.add(User(name, password = pwd))
        }

        val mapper = BaseMapper
            .fromList(users.toList())
            .to(UserDTO::class).ignore("password")

        val dtoList = mapper.adaptList()
        assert(dtoList.first().name == users.first().name)
    }

    @Test
    fun testInstanceBaseMapper() {
        data class User(val name: String, val password: String)
        data class UserDTO(val name: String?, val password: String?)

        val users = emptyList<User>().toMutableList()
        for (i in 0..2) {
            val name = faker.name.firstName()
            val pwd = "1234"
            users.add(User(name, password = pwd))
        }
        val mapper = BaseMapper<User, UserDTO>().to(UserDTO::class)


        assertThrows<UndefinedSourceObject> {
            mapper.adaptList()
        }
        val listDTOs = mapper.adaptList(users)
        assert(listDTOs.first().name==users.first().name)
    }
}