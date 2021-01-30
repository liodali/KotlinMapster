package mapper

import kotlin.IllegalArgumentException
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.memberProperties
import mapper.adaptListTo

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

fun <T : Any, R : Any> BaseMapper<T, R>.transformation(
    srcAttribute: String,
    transformation: TransformationExpression<T>
): BaseMapper<T, R> {
    val base = this
    base.configMapper.apply {
        this.transformation(srcAttribute) {
            transformation(it as T)
        }
    }
    return base
}

fun <T : Any, R : Any> BaseMapper<T, R>.ignoreIf(
    srcAttribute: String,
    expression: ConditionalIgnore<T>
): BaseMapper<T, R> {
    val base = this
    (base.configMapper as ConfigMapper<T, R>).ignoreIf(srcAttribute) {
        expression(it)
    }
    return base
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
        } /* else {
             /// TODO support list mapping
         }*/
        v
    }.toTypedArray()
    return dest.primaryConstructor!!.call(*fieldsArgs)
}

class BaseMapper<T : Any, R : Any> constructor() : IMapper<T, R> {
    private constructor(sourceList: List<T>?, source: T?) : this() {
        this.sourceListData = sourceList
        this.sourceData = source
    }

    private constructor(sourceList: List<T>) : this(sourceList, null)
    private constructor(source: T) : this(null, source)

    private var dest: KClass<*>? = null
    private lateinit var src: KClass<*>

    private var sourceData: T? = null
    private var sourceListData: List<T>? = null

    var configMapper: ConfigMapper<*, *> = ConfigMapper<Any, Any>()
        private set

    companion object {
        private lateinit var instance: BaseMapper<*, *>
        fun <T> from(source: T): BaseMapper<T, Any> where T : Any {
            instance = BaseMapper<T, Any>(source)
            instance.configMapper = ConfigMapper<T, Any>()
            return instance.from(source::class) as BaseMapper<T, Any>
        }

        fun <T : Any> fromList(list: List<T>): BaseMapper<T, Any> {
            instance = BaseMapper<T, Any>(list)
            instance.configMapper = ConfigMapper<T, Any>()
            val typeData = (list::class as KClass<T>).memberProperties.first()
                .returnType.arguments.first().type!!.classifier as KClass<T>
            return instance.from(typeData) as BaseMapper<T, Any>
        }
    }

    fun <T, R> newConfig(configMapper: ConfigMapper<T, R>): BaseMapper<T, R> where T : Any, R : Any {
        instance.configMapper = configMapper
        return instance as BaseMapper<T, R>
    }

    fun adapt(source: T? = sourceData): R {
        if (dest == null) {
            throw UndefinedDestinationObject
        }
        if (source != null) {
            sourceData = source
        } else {
            throw UndefinedSourceObject
        }
        if (configMapper.hasConfiguration()) {
            return sourceData!!.mapping(dest!!, configMapper) as R
        }
        return sourceData!!.adaptTo(dest!!) as R
    }

    fun adaptList(listSource: List<T>? = sourceListData): List<R> {

        if (dest == null) {
            throw UndefinedDestinationObject
        }
        if (listSource != null) {
            sourceListData = listSource
        } else {
            throw UndefinedSourceObject
        }
        if (configMapper.hasConfiguration()) {
            val list = emptyList<R>().toMutableList()
            sourceListData!!.forEach {
                list.add(it!!.mapping(dest!!, configMapper) as R)
            }
            return list.toList()
        }
        return sourceListData!!.adaptListTo(dest!!) as List<R>
    }

    override fun <R : Any> to(dest: KClass<R>): BaseMapper<T, R> {
        this.dest = dest
        return this as BaseMapper<T, R>
    }

    private fun <T : Any> from(src: KClass<T>): BaseMapper<T, R> {
        this.src = src
        return this as BaseMapper<T, R>
    }
}




