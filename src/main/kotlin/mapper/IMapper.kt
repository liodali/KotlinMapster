package mapper

import kotlin.reflect.KClass

internal typealias Expression<T> = (src: T) -> Boolean

inline fun <reified R : Any, T : Any> IMapper<T, R>.adapt(src: T?): R {
    if (this is BaseMapper) {
        return this.adapt(src)
    }
    return src?.adaptTo(R::class)
        ?: throw IllegalAccessException("you cannot map null object,configure BaseMapper correctly")
}

interface IMapper<T, R> {

    fun <R : Any> to(dest: KClass<R>): IMapper<*, R>
    fun <T : Any> from(src: KClass<T>): IMapper<T, *>
}
