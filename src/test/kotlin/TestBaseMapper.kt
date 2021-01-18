import mapper.BaseMapper
import mapper.ignore
import mapper.ignoreIf
import org.junit.jupiter.api.Test

class TestBaseMapper {

    @Test
    fun baseTest() {
        data class User(val name: String, val password: String)
        data class UserDTO(val name: String?, val password: String)

        val user = User("hamza", password = "dali")
        /* val config = ConfigMapper<User, UserDTO>()
         config.ignoreIf {
             it.name != "dali"
         }*/
        val mapper = BaseMapper.from(user).to(UserDTO::class)
            .ignoreIf("name") {
                it.name != "dali"
            }
        val dto = mapper.adapt()
        assert(dto == UserDTO("hamza", "dali"))
    }

    @Test
    fun baseIgnoreIfTest() {
        data class User(val name: String, val password: String)
        data class UserDTO(val name: String?, val password: String?)

        val user = User("dali", password = "1234")

        val mapper = BaseMapper.from(user).to(UserDTO::class)
            .ignoreIf("name") {
                it.name != "dali"
            }
        val dto = mapper.adapt()
        assert(dto == UserDTO(null, "1234"))

        val user1 = User("dali", password = "1234")
        mapper.ignore("password")
        val dtoUser1 = mapper.adapt(user1)
        assert(dtoUser1 == UserDTO("dali", null))

    }
}
