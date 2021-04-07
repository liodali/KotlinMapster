package mapper

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor

internal fun <T : Any, R : Any> BaseMapper<T, R>.verifyAttExist(
    attribute: String? = null,
    attributes: List<String>? = null,
    checkSrcAtt: Boolean = true
): Boolean {
    val base = this
    val typedChecked: KClass<*>? = when (checkSrcAtt) {
        true ->
            base.src
        else ->
            base.dest
    }
    return when {
        attributes != null -> {
            val isCheckPrimaryAtt = typedChecked?.primaryConstructor!!.parameters.map {
                it.name
            }.toList().containsAll(attributes)
            checkExistingNestedAtt(
                isCheckPrimaryAtt,
                typedChecked,
                attributes
            )
        }
        attribute != null -> {
            val isCheckPrimaryAtt = typedChecked?.primaryConstructor!!.parameters.map {
                it.name
            }.toList().contains(attribute)
            checkExistingNestedAtt(
                isCheckPrimaryAtt,
                typedChecked,
                attribute = attribute
            )
        }
        else -> false
    }
}

private fun checkExistingNestedAtt(
    isCheckPrimaryAtt: Boolean,
    typedChecked: KClass<*>,
    attributes: List<String>? = null,
    attribute: String? = null
): Boolean {
    if (!isCheckPrimaryAtt) {

        val listAttributes = typedChecked.primaryConstructor!!.parameters.asSequence().filter {
            (it.type.classifier as KClass<*>).isData
        }.map {
            it.type.classifier as KClass<*>
        }.map {
            it.primaryConstructor!!.parameters
        }.flatten().map {
            it.name
        }.toList()
        return when {
            attributes != null -> listAttributes.containsAll(attributes)
            attribute != null -> listAttributes.contains(attribute)
            else -> false
        }
    }
    return isCheckPrimaryAtt
}

internal fun Any.getFieldValue(
    src: KClass<Any>,
    destName: String,
    mappedName: Any?,
    prop: KProperty1<Any, *>? = null,
    isNested: Boolean = false,
): Pair<Any?, KProperty1<Any, *>> {
    val field = src.declaredMemberProperties.firstOrNull {
        when (mappedName) {
            is Array<*> -> {
                mappedName.contains(it.name)
            }
            else -> {
                val name = mappedName ?: destName
                it.name == name
            }
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
