package mapper

import java.lang.IllegalStateException
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.superclasses
import kotlin.reflect.jvm.jvmName
import mapper.annotations.CombineTo
import mapper.annotations.MapTo

// / adaptTo : mapper from T to R
// / runtime mapper
// / [dest] : class type of destination object
fun <T : Any, R : Any> T.adaptTo(dest: KClass<R>): R {
    if (dest.isData) {
        val (fieldsAnnotationsCombineTo, combineToFieldsValues) = this.findAllAnnotationsField<CombineTo>()
        val (fieldsAnnotationsMapTo, mapToFieldsValues) = this.findAllAnnotationsField<MapTo>()
        val argsValues = dest.primaryConstructor!!.parameters.map { p ->

            val fields = fieldsAnnotationsCombineTo.filter { props ->
                val annotation = props.findAnnotation<CombineTo>()
                (annotation as CombineTo).destAtt == p.name
            }

            var v = if (fields.isEmpty()) {
                val field = fieldsAnnotationsMapTo.firstOrNull {
                    var name = it.name
                    val mapTo = it.findAnnotation<MapTo>()
                    if (mapTo != null) {
                        name = mapTo.destAttName
                    }
                    p.name.equals(name)
                } ?: (this::class as KClass<Any>).declaredMemberProperties.first { it.name == p.name }
                if (mapToFieldsValues.containsKey(field.name)) {
                    mapToFieldsValues[field.name]
                } else {
                    field!!.get(this)
                }
            } else {
                if ((p.type.classifier as KClass<*>).jvmName == "java.lang.String") {

                    fields.sortedBy {
                        it.findAnnotation<CombineTo>()!!.index
                    }.fold("") { acc, prop ->
                        if (acc.isNotEmpty())
                            "$acc${prop.findAnnotation<CombineTo>()!!.separator}${combineToFieldsValues[prop.name]}"
                        else
                            "${combineToFieldsValues[prop.name]}"
                    }
                } else {
                    throw IllegalStateException("Annotation CombineTo supported only with String type")
                }
            }
            if ((p.type.classifier as KClass<*>).isData) {
                v = v!!.adaptTo(
                    p.type.classifier as KClass<*>
                )
            } else {
                if ((p.type.classifier as KClass<*>).superclasses.contains(Collection::class)) {
                    val typeDest =
                        (dest.memberProperties.first { td -> td.name == p.name }.returnType)
                            .arguments.first().type!!.classifier as KClass<*>
                    v = (v!! as Collection<Any>).adaptListTo(typeDest)
                }
            }
            v
        }.toTypedArray()
        combineToFieldsValues.clear()
        return dest.primaryConstructor!!.call(*argsValues)
    }
    throw UnSupportedMappingType
}

fun <T : Any, R : Any> Collection<T>.adaptListTo(dest: KClass<R>): Collection<R> {
    val listDest = emptyList<R>().toMutableList()
    this.forEach {
        listDest.add(it.adaptTo(dest))
    }

    return listDest
}

private inline fun <reified T : Annotation> Any.findAllAnnotationsField(): Pair<List<KProperty1<Any, *>>, HashMap<String, Any>> {
    val fieldsAnnotations = emptyList<KProperty1<Any, *>>().toMutableList()
    val fieldsValues = HashMap<String, Any>()
    (this::class as KClass<*>).declaredMemberProperties.forEach { prop ->
        if ((prop.returnType.classifier as KClass<*>).isData) {
            (prop.returnType.classifier as KClass<*>).declaredMemberProperties.filter { subProp ->
                subProp.annotations.isNotEmpty()
            }.map { subProp ->
                val annotation = subProp.findAnnotation<T>()
                if (annotation != null) {
                    fieldsAnnotations.add(subProp as KProperty1<Any, *>)
                    fieldsValues[subProp.name] = subProp.getValue((prop as KProperty1<Any, *>).get(this)!!, subProp)!!
                }
            }
        } else {
            val annotation = prop.findAnnotation<T>()
            if (annotation != null) {
                fieldsAnnotations.add(prop as KProperty1<Any, *>)
                fieldsValues[prop.name] = prop.get(this)!!
            }
        }
    }
    return Pair(fieldsAnnotations, fieldsValues)
}
