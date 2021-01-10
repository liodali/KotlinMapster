## Mapster Kotlin <p style="font-size:18px">(Experimental)</p>

* Fun and easily mapper from object to another
* Runtime mapping
* Nested mapping
* Array & List mapping(available soon)

`version : 0.1.1`

### Gradle Installation
```groovy
repositories {
    maven {
        url = "https://maven.pkg.github.com/liodali/KotlinMapster"
        credentials {
            username = "liodali"
            password = "b956e329e8b80286465c34628180a08b570108e7"
        }
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
