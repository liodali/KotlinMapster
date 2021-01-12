package mapper.annotations

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class CombineTo(
    val destAtt: String,
    val index: Int = 0,
    val separator: String = " "
)
