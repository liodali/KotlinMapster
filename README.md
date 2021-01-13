## Mapster Kotlin <p style="font-size:18px">(Experimental)</p>

* Fun and easily mapper from object to another
* Runtime mapping
* Nested mapping
* Array & List mapping

`version : 0.2.1`

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

### PS

* To create Github personal token follow this link and also you need to choose `read:packages` :
    * https://docs.github.com/en/free-pro-team@latest/github/authenticating-to-github/creating-a-personal-access-token
