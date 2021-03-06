import io.github.serpro69.kfaker.Faker
import mapper.BaseMapper
import mapper.IMapper
import mapper.adaptList
import mapper.adaptListInverse
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
            .to(UserDTO::class)
            .transformation(
                "fullName"
            ) { u ->
                u.firstName + " " + u.lastName
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

    @Test
    fun testInverseNestedDataMapping() {
        data class Address(
            val street: String,
            val city: String,
            val country: String
        )

        data class User(
            val firstName: String,
            val lastName: String,
            val password: String,
            val address: Address,
        )

        data class UserDTO(
            val fullName: String,
            val password: String,
            val fullAddress: String,
        )

        val name = faker.name.firstName()
        val lastName = faker.name.lastName()
        val pwd = "1234"
        val streetAddress = faker.address.streetName()
        val cityAddress = faker.address.city()
        val countryAddress = faker.address.country()

        val user = User(
            name,
            lastName,
            password = pwd,
            address = Address(
                streetAddress,
                cityAddress,
                countryAddress
            )
        )

        val mapper = BaseMapper.from(user)
            .to(UserDTO::class)
            .transformation(
                "fullName"
            ) { u ->
                u.firstName + " " + u.lastName
            }.transformation("fullAddress") { u ->
                "${u.address.street},${u.address.city},${u.address.country}"
            }.inverseTransformation(
                "firstName"
            ) { dto ->
                dto.fullName.split(" ").first()
            }.inverseTransformation(
                "lastName"
            ) { dto ->
                dto.fullName.split(" ").last()
            }.inverseTransformation(
                "street"
            ) { dto ->
                dto.fullAddress.split(",").first()
            }.inverseTransformation(
                "city"
            ) { dto ->
                dto.fullAddress.split(",")[1]
            }.inverseTransformation(
                "country"
            ) { dto ->
                dto.fullAddress.split(",").last()
            }
            .mapMultiple(arrayOf("firstName", "lastName"), "fullName")
            .mapTo("address", "fullAddress")
            .mapMultiple(arrayOf("street", "city", "country"), "fullAddress")

        val dto = mapper.adapt()
        val orignalObject = mapper.adaptInverse(dto)
        assert(orignalObject == user)
    }

    @Test
    fun testInverseListMapping() {
        data class User(val firstName: String, val lastName: String, val password: String)
        data class UserDTO(val fullName: String, val password: String)

        val listUser = emptyList<User>().toMutableList()

        (1..3).forEach { _ ->

            val name = faker.name.firstName()
            val lastName = faker.name.lastName()
            val pwd = "1234"
            listUser.add(User(name, lastName, password = pwd))
        }
        val mapper: IMapper<User, UserDTO> = BaseMapper<User, UserDTO>()
            .from(User::class)
            .to(UserDTO::class)
            .transformation(
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

        val listDTOs = mapper.adaptList(listUser)

        val listInverse = mapper.adaptListInverse(listDTOs)
        assert(listInverse == listUser)
    }
}
