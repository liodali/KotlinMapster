import io.github.serpro69.kfaker.Faker
import mapper.BaseMapper
import mapper.IMapper
import mapper.UndefinedSourceObject
import mapper.adapt
import mapper.adaptList
import mapper.ignore
import mapper.mapTo
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
        assert(listDTOs.first().name == users.first().name)
    }

    @Test
    fun testNestedListMapping() {

        data class Address(val addressName: String, val country: String)
        data class AddressDTO(val addressName: String, val country: String)
        data class User(val name: String, val password: String, val listAdr: List<Address>)
        data class UserDTO(val name: String?, val password: String?, val listAdr: List<AddressDTO>)

        val users = emptyList<User>().toMutableList()
        for (i in 0..2) {
            val name = faker.name.firstName()
            val pwd = "1234"
            val nameAdr = faker.address.streetName()
            val country = faker.address.country()
            users.add(User(name, password = pwd, listAdr = listOf(Address(nameAdr, country))))
        }
        val mapper: IMapper<User, UserDTO> = BaseMapper<User, UserDTO>().to(UserDTO::class)
            .nestedTransformation<Address>(srcAttribute = "country") {
                it.country.subSequence(0, 3)
            }
        val listDTO = mapper.adaptList(users)

        assert(listDTO.first().listAdr.first().country.length == 3)
    }

    @Test
    fun testNestedMapping() {
        data class Address(val addressName: String, val country: String)
        data class User(val name: String, val email: String, val password: String, val address: Address)
        data class LoginDTO(val login: String?, val password: String?, val country: String)
        /*
         * data preparation
         */
        val name = faker.name.firstName()
        val email = faker.internet.email()
        val country = faker.address.country()
        val nameAdr = faker.address.streetName()
        val user = User(name, email, "1234", Address(nameAdr, country))
        /*
         * BaseMapper builder with mapTo and transformation
         */
        val mapper: IMapper<User, LoginDTO> = BaseMapper.from(user).to(LoginDTO::class)
            .mapTo("email", "login")
            .nestedTransformation<Address>("country") {
                it.country.toUpperCase().subSequence(0, 3).toString()
            }

        val dto = mapper.adapt()
        assert(dto.country == user.address.country.toUpperCase().subSequence(0, 3).toString())
    }
}
