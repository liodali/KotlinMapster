package mapper

import kotlin.IllegalArgumentException
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.superclasses

fun <T : Any, R : Any> BaseMapper<T, R>.ignore(
    srcAttribute: String
):
        BaseMapper<T, R> {
    val base = this
    base.configMapper.apply {
        this.ignoreAtt(srcAttribute)
    }
    return base
}

fun <T : Any, R : Any> BaseMapper<T, R>.mapMultiple(
    from: Array<String>,
    to: String
): BaseMapper<T, R> {
    val base = this
    base.configMapper.apply {
        this.map(from, to)
    }
    return base
}

fun <T : Any, R : Any> BaseMapper<T, R>.mapTo(
    from: String,
    to: String
): BaseMapper<T, R> {
    val base = this
    base.configMapper.apply {
        this.map(arrayOf(from), to)
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

fun <T : Any, R : Any> BaseMapper<T, R>.inverseTransformation(
    attribute: String,
    transformation: TransformationExpression<R>
): BaseMapper<T, R> {
    val base = this
    base.configMapper.apply {
        this.inverseTransformation(attribute) { e ->
            transformation(e as R)
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

internal fun Any.getParentFieldValue(
    src: KClass<Any>,
    destName: String,
    prop: KProperty1<Any, *>? = null,
    isNested: Boolean = false,
): Any? {
    val field = src.declaredMemberProperties.firstOrNull {
        it.name == destName
    }
    if (field == null) {
        src.declaredMemberProperties.forEach { p ->
            if ((p.returnType.classifier as KClass<*>).isData) {
                return this.getParentFieldValue(
                    (p.returnType.classifier as KClass<Any>),
                    destName,
                    prop = p,
                    isNested = true
                )
            }
        }
    }
    if (isNested) {
        return (prop as KProperty1<Any, *>).get(this)
    }
    return field!!.get(this)
}

internal fun Any.getFieldValue(
    src: KClass<Any>,
    destName: String,
    mappedName: Any?,
    prop: KProperty1<Any, *>? = null,
    isNested: Boolean = false,
): Pair<Any?, KProperty1<Any, *>> {
    val field = src.declaredMemberProperties.firstOrNull {
        if (mappedName is Array<*>) {
            mappedName.contains(it.name)
        } else {
            val name = mappedName ?: destName
            it.name == name
        }
    }

    if (field == null) {
        src.declaredMemberProperties.forEach { p ->
            if ((p.returnType.classifier as KClass<*>).isData) {
                return this.getFieldValue(
                    (p.returnType.classifier as KClass<Any>),
                    destName,
                    mappedName,
                    prop = p,
                    isNested = true
                )
            }
        }
    }
    if (isNested) {
        return Pair(field!!.getValue((prop as KProperty1<Any, *>).get(this)!!, field)!!, field)
    }
    return Pair(field!!.get(this), field)
}

private fun <T : Any> T.mapping(
    dest: KClass<*>,
    configMapper: ConfigMapper<*, *>,
    isNested: Boolean = false,
    isBackward: Boolean = false
): Any {

    val listExpressions: List<Pair<String, ConditionalIgnore<T>>> =
        configMapper.listIgnoredExpression as List<Pair<String, ConditionalIgnore<T>>>
    val listAtt: List<String> = configMapper.listIgnoredAttribute
    val listMappedAtt: List<Pair<Array<String>, String>> = configMapper.listMappedAttributes
    val listTransformations =
        configMapper.listTransformationExpression as List<Pair<String, TransformationExpression<T>>>
    val listNestedTransformation =
        configMapper.listNestedTransformationExpression as List<Pair<String, TransformationExpression<Any>>>
    val listInverseTransformation =
        configMapper.listInverseTransformationExpression as List<Pair<String, TransformationExpression<Any>>>

    val fieldsArgs = dest.primaryConstructor!!.parameters.map { kProp ->

        val nameMapper: Any? = listMappedAtt.firstOrNull { m ->
            if (isBackward) m.first.contains(kProp.name) else m.second == kProp.name
        }?.let {
            if (isBackward)
                it.second
            else
                it.first
        }


        val (value, field) = this.getFieldValue((this::class as KClass<Any>), kProp.name!!, nameMapper)

        var v: Any? = value
        val isIgnore = listExpressions.map {
            !it.second(this) && field.name == it.first
        }.firstOrNull { it } != null || listAtt.contains(field.name)
        if (isIgnore && !isBackward) {
            if (kProp.type.isMarkedNullable) {
                v = null
            } else
                throw IllegalArgumentException("you cannot ignore non nullable field")
        } else if (!isBackward) {
            if (listTransformations.isNotEmpty()) {
                v = listTransformations.firstOrNull {
                    it.first == field.name
                }?.let { transformation ->
                    transformation.second(this)
                } ?: v
            } else if (listNestedTransformation.isNotEmpty()) {
                v = listNestedTransformation.firstOrNull {
                    it.first == field.name
                }?.let { pair ->
                    val nestedV: Any? = if (isNested) {
                        this
                    } else {
                        this.getParentFieldValue((this::class as KClass<Any>), pair.first)
                    }
                    pair.second(nestedV!!)
                } ?: v
            }
        } else {
            if (listInverseTransformation.isNotEmpty()) {
                v = listInverseTransformation.firstOrNull {
                    it.first == kProp.name
                }?.let {
                    it.second(this)
                } ?: v
            }

        }
        if ((kProp.type.classifier as KClass<*>).isData) {
            v = v!!.mapping(kProp.type.classifier as KClass<*>, configMapper, isNested = true, isBackward = isBackward)
        } else {
            if ((kProp.type.classifier as KClass<*>).superclasses.contains(Collection::class)) {
                val typeDest = (kProp.type).arguments.first().type!!.classifier as KClass<*>
                val baseList: BaseMapper<*, *> = BaseMapper<Any, Any>()
                    .from((kProp.type.classifier as KClass<*>))
                baseList.newConfig(configMapper)
                baseList.isNested = true
                baseList.isBackward = isBackward
                v = baseList.to(typeDest).adaptList(v as List<Nothing>)
            }
        }
        v
    }.toTypedArray()
    return dest.primaryConstructor!!.call(*fieldsArgs)
}

class BaseMapper<T : Any, R : Any> : IMapper<T, R> {
    constructor() {
        instance = this
    }

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

    internal var isNested = false
    internal var isBackward = false

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

    /*
    * [adaptInverse] : method to inverse mapping from R to T
    *
     */
    fun adaptInverse(source: R): T {
        if (configMapper.hasConfiguration()) {
            return source.mapping(src, configMapper, isBackward = true) as T
        }
        return source.adaptTo(src) as T
    }

    fun adapt(source: T? = sourceData): R {
        if (dest == null) {
            throw UndefinedDestinationObject
        }
        if (source == null) {
            throw UndefinedSourceObject
        }
        sourceData = source
        if (configMapper.hasConfiguration()) {
            return sourceData!!.mapping(dest!!, configMapper) as R
        }
        return sourceData!!.adaptTo(dest!!) as R
    }

    fun adaptList(listSource: List<T>? = sourceListData): List<R> {

        if (dest == null) {
            throw UndefinedDestinationObject
        }
        if (listSource == null) {
            throw UndefinedSourceObject
        }
        sourceListData = listSource
        if (sourceListData!!.isEmpty()) {
            return emptyList()
        }
        if (configMapper.hasConfiguration()) {
            val list = emptyList<R>().toMutableList()
            sourceListData!!.forEach {
                list.add(it!!.mapping(dest!!, configMapper, isNested = isNested) as R)
            }
            return list.toList()
        }
        return sourceListData!!.adaptListTo(dest!!) as List<R>
    }

    fun <K : Any> nestedTransformation(
        srcAttribute: String,
        nestedTransformation: TransformationExpression<K>
    ): BaseMapper<T, R> {
        val base = this
        base.configMapper.apply {
            this.nestedTransformation<K>(srcAttribute) {
                nestedTransformation(it)
            }
        }
        return base
    }

    override fun <R : Any> to(dest: KClass<R>): BaseMapper<T, R> {
        this.dest = dest
        return this as BaseMapper<T, R>
    }

    internal fun <T : Any> from(src: KClass<T>): BaseMapper<T, R> {
        this.src = src
        return this as BaseMapper<T, R>
    }
}
