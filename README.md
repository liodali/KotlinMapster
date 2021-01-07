## Mapster Kotlin <p style="font-size:18px">(Experimental)</p>


* Fun and easily mapper from object to another 
* Runtime mapping
* Nested mapping
* Array & List mapping(available soon)

### simple example
```kotlin
data class Person(val email: String, val password: String, val firstName: String)
data class PersonDTO(val email: String, val firstName: String)

val person = Person(
    "lorem@email.com", "person", "person", Address(
        "000", "new york", "new york", 2010
    )
)

val dto = person.adaptTo(PersonDTO::class)
```
