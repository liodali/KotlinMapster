package mapper

import kotlin.reflect.KClass

/**
 * Annotation : [CombineTo]
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class CombineTo(
    val destinationAttribute: String,
    val index: Int = 0,
    val separator: String = " "
)


@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class MapTo(val destinationAttributeName: String)


@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class  TransformationFrom(val fromAttributes:Array<String>,val converter:KClass<MapsterConverter<*,*>>)