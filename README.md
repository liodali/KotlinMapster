## Mapster Kotlin <p style="font-size:18px">(Experimental)</p>

* Fun and easily mapper from object to another
* Runtime mapping
* Nested mapping
* Array & List mapping

`stable-version : 0.3.1`

`dev-version : 0.4.0-beta02`

### Gradle Installation

```groovy
repositories {
    // using github packages
    maven {
        url = "https://maven.pkg.github.com/liodali/KotlinMapster"
        credentials {
            username = "YOUR-USERNAME"
            password = "YOUR-TOKEN-GITHUB"
        }
    }
    // or jcenter bintray
    maven {
        url = uri("https://dl.bintray.com/liodali/KotlinMapster")
    }
}
dependencies {
    implementations "com.dali.hamza:mapster-ktx:version"
}
```

### simple example :

```kotlin
data class Person(val email: String, val password: String, val firstName: String)
data class PersonDTO(val email: String, val firstName: String)

val person = Person("lorem@email.com", "person", "person",)

val dto = person.adaptTo(PersonDTO::class)
```

### mapping list example :

```kotlin
data class Person(val email: String, val password: String, val firstName: String)
data class PersonDTO(val email: String, val firstName: String)

val persons = listOf(Person("lorem@email.com", "person", "person",), Person("lorem@email.com", "person", "person",))

val dtos = persons.adaptListTo(PersonDTO::class)
```

### Basic Annotation :

* use `MapTo` annotation to map from an attribute to another with difference name

```kotlin
 data class Person(@MapTo("login") val email: String, val password: String, val firstName: String, val adr: Address)

data class LoginUser(val login: String, val password: String)

val login = person.adaptTo(LoginUser::class)


```

### Properties `MapTo`

Attribute     | description | 
--------------| ------------|
`destAttName` | (String) name of attribute destination         | 

<br>

-------------------------
<br>

* use `CombineTo` annotation to combine attributes to another with difference name

```kotlin
data class User(
    @CombineTo(destAtt = "fullName", index = 0) val firstName: String,
    @CombineTo(destAtt = "fullName", index = 1) val lastName: String,
    val CIN: String
)

data class UserDTO(
    val fullName: String,
    val CIN: String
)

val dto = user.adaptTo(UserDTO::class)

```

### Properties `CombineTo`

-------------------------

Attribute     | description | 
--------------| ------------|
`destAtt`     | (String) name of attribute destination         | 
`separator`   | (String) separator between the combined values       | 
`index`       | (Int)  position in final result       | 

### Advanced Examples

* `BaseMapper` : mapper instance
    * you can use `IMapper` interface to pass it into a DI
    * support list mapping
    * support nested list mapping and nested Transformation
    * available in 0.4.0-alpha

```kotlin

data class User(val name: String, val password: String, val country: String, val phone: String)
data class UserDTO(val name: String, val password: String)

val faker = Faker() // faker object to generate random data
val user = User(
    name = faker.name.firstName(),
    password = "1234",
    country = faker.address.country(),
    phone = faker.phoneNumber.phoneNumber()
)

val mapper = BaseMapper.from(user).to(UserDTO::class)
val dto = mapper.adapt()
```

### Map List of object

```kotlin
data class User(val name: String, val password: String)
data class UserDTO(val name: String?, val password: String?)

val users = emptyList<User>().toMutableList()
for (i in 0..2) {
    val name = faker.name.firstName()
    val pwd = "1234"
    users.add(User(name, password = pwd))
}
// new way to create instance of BaseMapper
val mapper = BaseMapper<User, UserDTO>()
    .to(UserDTO::class).ignore("password")

val dtoList = mapper.adaptList(users)
```

## Mapper Manipulation

* you can create custom configuration for `BaseMapper` to manipulate data during mapping

```kotlin
data class User(val name: String, val password: String, val dateCreation: String, val age: Int)
data class UserDTO(val name: String?, val password: String?, val dateCreation: String?, val age: Int?)

val name = faker.name.firstName()
val user = User(name, password = "1234", "12/12/2020", 20)

/// ConfigMapper Instance
val configMapper = ConfigMapper<User, UserDTO>()
    .ignoreAtt("age") // ignore field
    .ignoreIf("dateCreation") {     // conditional ignore
        it.dateCreation.isEmpty()  //
    }
    .transformation("password") { user -> //transformation
        hashPassword(user.password)
    }.map("name", "login") // map field to another destination field
/// BaseMapper Instance
val mapper = BaseMapper.from(user).to(UserDTO::class).newConfig(configMapper)
/// map user to dto
val dto = mapper.adapt()
```

* you can apply the same manipulation use `BaseMapper` without need to create new `ConfigurationMapper`

### Ignore

    To ignore Field, you need to mark it nullable

* Ignore Field :

```kotlin
data class User(val name: String, val password: String)
data class UserDTO(val name: String?, val password: String?)

val name = faker.name.firstName()
val user = User(name, password = "1234")
val mapper = BaseMapper.from(user).to(UserDTO::class)
    .ignore("password")
val dto = mapper.adapt()
```

* Conditional Ignore Field :

  > You can ignore Field Conditionally with condition base on source, when condition is met,the field that has the same name in destination object will be skipped.

  > You can combine it with map to skip field that has different name in destination object.

```kotlin
data class User(val name: String, val password: String)
data class UserDTO(val name: String?, val password: String?)

val name = faker.name.firstName()

val user = User(name, password = "1234")
val mapper = BaseMapper.from(user).to(UserDTO::class)
    .ignore("password")
val dto = mapper.adapt()
  ```

### Transformation :

> you can compute new values using transformation,example hash the password entered by the user

```kotlin
data class User(val name: String, val email: String, val password: String, val country: String)
data class LoginDTO(val login: String?, val password: String?)
// data preparation
val name = faker.name.firstName()
val email = faker.internet.email()
val country = faker.address.country()
val user = User(name, email, "1234", country)

//BaseMapper builder with mapTo and transformation 
val mapper = BaseMapper.from(user).to(LoginDTO::class)
    .mapTo("email", "login")
    .transformation("password") { user ->
        hashPassword(user.password)
    }
val dto = mapper.adapt()
```

### MapTo

> you can map field with difference names using `MapTo`

```kotlin

data class User(val name: String, val email: String, val password: String, val country: String)
data class LoginDTO(val login: String?, val password: String?)

val user = User(faker.name.firstName(), faker.internet.email(), password = "1234", faker.address.country())

val mapper = BaseMapper.from(user).to(UserDTO::class)
    .mapTo("email", "login")
val dto = mapper.adapt()
```

### PS

* To create Github personal token follow this link and also you need to choose `read:packages` :
    * https://docs.github.com/en/free-pro-team@latest/github/authenticating-to-github/creating-a-personal-access-token
