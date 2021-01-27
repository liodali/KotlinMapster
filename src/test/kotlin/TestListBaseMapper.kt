import io.github.serpro69.kfaker.Faker
import mapper.BaseMapper
import mapper.adaptListTo
import mapper.ignore
import org.junit.jupiter.api.Test

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
        users.adaptListTo(UserDTO::class)

        val mapper = BaseMapper
            .fromList(users.toList())
            .to(UserDTO::class).ignore("password")

        val dtoList = mapper.adaptList()
        assert(dtoList.first().name == users.first().name)
    }
}