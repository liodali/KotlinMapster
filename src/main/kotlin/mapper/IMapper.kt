package mapper

import kotlin.reflect.KClass

inline fun <reified R : Any, reified T : Any> IMapper<T, R>.adaptInverse(src: R): T {
    if (this is BaseMapper) {
        return this.adaptInverse(src)
    }
    return src.adaptTo(T::class)
}

inline fun <reified R : Any, T : Any> IMapper<T, R>.adapt(src: T? = null): R {
    if (this is BaseMapper) {
        return if (src != null)
            this.adapt(src)
        else return this.adapt()
    }
    return src?.adaptTo(R::class)
        ?: throw IllegalAccessException("you cannot map null object,configure BaseMapper correctly")
}

inline fun <reified R : Any, reified T : Any> IMapper<T, R>.adaptListInverse(src: List<R>): List<T> {
    if (this is BaseMapper) {
        return this.adaptListInverse(src)
    }
    return src.adaptListTo(T::class).toList()
}

inline fun <reified R : Any, T : Any> IMapper<T, R>.adaptList(src: List<T> = emptyList()): List<R> {
    if (this is BaseMapper) {
        return if (src.isEmpty())
            this.adaptList()
        else this.adaptList(src)
    }
    return src.adaptListTo(R::class).toList()
}

interface IMapper<T, R> {

    fun <R : Any> to(dest: KClass<R>): IMapper<*, R>
}
