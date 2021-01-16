package mapper

import kotlin.reflect.KClass

/*inline fun <reified T : Any> BaseMapper.ignoreIf(crossinline expression: Expression<T>): BaseMapper {
    val base = this
    if (T::class.simpleName == this.src.simpleName) {
        base.configMapper.apply {
            this.ignoreIf {
                expression(it as T)
            }
        }
    }
    return base
}*/

fun BaseMapper<*, *>.ignore(srcAttribute: String): BaseMapper<*, *> {
    val base = this
    base.configMapper.apply {
        this.ignoreAtt(srcAttribute)
    }
    return base
}

inline fun <T, R> BaseMapper<T, R>.ignoreIf(crossinline expression: Expression<T>): BaseMapper<T, R> {
    val base = this
    base.configMapper = this.configMapper.apply {
        this.ignoreIf {
            expression(it as T)
        }
    }
    return base
}

class BaseMapper<T, R> : IMapper<T, R> {
    lateinit var dest: KClass<*>
        private set
    lateinit var src: KClass<*>
        private set
    var configMapper: ConfigMapper<*, *> = ConfigMapper<Any, Any>()

    companion object {
        private lateinit var instance: BaseMapper<*, *>
        fun <T> from(source: T): BaseMapper<T, *> where T : Any {
            instance = BaseMapper<T, Any>()
            instance.configMapper = ConfigMapper<T, Any>()
            return instance.from(source::class) as BaseMapper<T, *>
        }

        fun <T, R> newConfig(configMapper: ConfigMapper<T, R>): BaseMapper<T, R> where T : Any, R : Any {
            instance = BaseMapper<T, R>()
            instance.configMapper = ConfigMapper<T, R>()
            return instance as BaseMapper<T, R>
        }
    }

    fun adapt(): R {
        return src.adaptTo(dest) as R
    }

    override fun <R : Any> to(dest: KClass<R>): BaseMapper<T, R> {
        this.dest = dest
        return this as BaseMapper<T, R>
    }

    override fun <T : Any> from(src: KClass<T>): BaseMapper<T, R> {
        this.src = src
        return this as BaseMapper<T, R>
    }
}
