package mapper

import kotlin.IllegalArgumentException
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor

fun <T : Any, R : Any> BaseMapper<T, R>.ignore(srcAttribute: String): BaseMapper<T, R> {
    val base = this
    base.configMapper.apply {
        this.ignoreAtt(srcAttribute)
    }
    return base
}

fun <T : Any, R : Any> BaseMapper<T, R>.mapTo(srcAttribute: String, destAttribute: String): BaseMapper<T, R> {
    val base = this
    base.configMapper.apply {
        this.map(srcAttribute, destAttribute)
    }
    return base
}

inline fun <T : Any> BaseMapper<T, *>.transformation(
    srcAttribute: String,
    crossinline transformation: TransformationExpression<T>
): BaseMapper<*, *> {
    val base = this
    base.configMapper.apply {
        this.transformation(srcAttribute) {
            transformation(it as T)
        }
    }
    return base
}

inline fun <T, R> BaseMapper<T, R>.ignoreIf(
    srcAttribute: String,
    crossinline expression: ConditionalIgnore<T>
): BaseMapper<T, R> {
    val base = this
    val config = base.configMapper.ignoreIf(srcAttribute) {
        expression(it as T)
    }
    return base.newConfig(config) as BaseMapper<T, R>
}

private fun <T : Any> T.mapping(
    dest: KClass<*>,
    configMapper: ConfigMapper<*, *>
): Any {

    val listExpressions: List<Pair<String, ConditionalIgnore<T>>> =
        configMapper.listIgnoredExpression as List<Pair<String, ConditionalIgnore<T>>>
    val listAtt: List<String> = configMapper.listIgnoredAttribute
    val listMappedAtt: List<Pair<String, String>> = configMapper.listMappedAttributes
    val listTransformations =
        configMapper.listTransformationExpression as List<Pair<String, TransformationExpression<T>>>

    val fieldsArgs = dest.primaryConstructor!!.parameters.map { kProp ->

        val nameMapper: String? = listMappedAtt.firstOrNull { m ->
            m.second == kProp.name
        }?.first
        val field = (this::class as KClass<Any>).declaredMemberProperties.first {
            val name = nameMapper ?: kProp.name
            it.name == name
        }
        var v: Any? = field.get(this)
        val isIgnore = listExpressions.map {
            !it.second(this) && field.name == it.first
        }.firstOrNull { it } != null || listAtt.contains(field.name)
        if (isIgnore) {
            if (kProp.type.isMarkedNullable) {
                v = null
            } else
                throw IllegalArgumentException("you cannot ignore non nullable field")
        } else {
            if (listTransformations.isNotEmpty()) {
                v = listTransformations.firstOrNull {
                    it.first == field.name
                }?.let {
                    val newValue = it.second(this)
                    newValue
                } ?: v
            }
        }
         if ((kProp.type.classifier as KClass<*>).isData) {
             v = v!!.mapping(kProp.type.classifier as KClass<*>, configMapper)
         }/* else {
             /// TODO support list mapping
         }*/
        v
    }.toTypedArray()
    return dest.primaryConstructor!!.call(*fieldsArgs)
}

class BaseMapper<T, R> private constructor(source: T) : IMapper<T, R> {

    private var dest: KClass<*>? = null
    private lateinit var src: KClass<*>

    private var sourceData: T? = source
    var configMapper: ConfigMapper<*, *> = ConfigMapper<Any, Any>()
        private set

    companion object {
        private lateinit var instance: BaseMapper<*, *>
        fun <T> from(source: T): BaseMapper<T, *> where T : Any {
            instance = BaseMapper<T, Any>(source)
            instance.configMapper = ConfigMapper<T, Any>()
            return instance.from(source::class) as BaseMapper<T, *>
        }
    }

    fun <T, R> newConfig(configMapper: ConfigMapper<T, R>): BaseMapper<T, R> where T : Any, R : Any {
        instance.configMapper = configMapper
        return instance as BaseMapper<T, R>
    }

    fun adapt(source: T? = null): R {
        if (dest == null) {
            throw IllegalArgumentException("you cannot map to undefined object")
        }
        if (source == null && sourceData == null) {
            throw IllegalArgumentException("you cannot map from null object")
        } else {
            if (source != null) {
                if ((source!!)::class.simpleName != src.simpleName) {
                    throw IllegalArgumentException("difference source object")
                }
                sourceData = source
            }
        }
        if (configMapper.hasConfiguration()) {
            return sourceData!!.mapping(dest!!, configMapper) as R
        }
        return sourceData!!.adaptTo(dest!!) as R
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
