package mapper

import java.lang.IllegalStateException
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.jvmName
import mapper.annotations.CombineTo
import mapper.annotations.MapTo
import kotlin.reflect.full.*

fun <T : Any, R : Any> T.adaptTo(dest: KClass<R>): R {
    if (dest.isData) {
        val fieldsAnnotations = emptyList<KProperty1<Any, *>>().toMutableList()
        val fieldsValues = HashMap<String, Any>()
        (this::class as KClass<Any>).declaredMemberProperties.forEach { prop ->
            if ((prop.returnType.classifier as KClass<*>).isData) {
                (prop.returnType.classifier as KClass<*>).declaredMemberProperties.filter { subProp ->
                    subProp.hasAnnotation<CombineTo>()
                }.map {subProp->
                    val annotation = subProp.findAnnotation<CombineTo>()
                    if (annotation != null) {
                        fieldsAnnotations.add(subProp as KProperty1<Any, *>)
                        fieldsValues[subProp.name] = subProp.getValue(prop.get(this)!!, subProp)!!
                    }
                }

            } else {
                val annotation = prop.findAnnotation<CombineTo>()
                if (annotation != null) {
                    fieldsAnnotations.add(prop)
                    fieldsValues[prop.name] = prop.get(this)!!
                }
            }
        }
        val argsValues = dest.primaryConstructor!!.parameters.map { p ->

            val fields = fieldsAnnotations.filter { props ->
                val annotation = props.findAnnotation<CombineTo>()
                (annotation as CombineTo).destAtt == p.name
            }

            var v = if (fields.isEmpty()) {
                val field = (this::class as KClass<Any>).declaredMemberProperties.first {
                    var name = it.name
                    val mapTo = it.findAnnotation<MapTo>()
                    if (mapTo != null) {
                        name = mapTo.destAttName
                    }
                    p.name.equals(name)
                }
                field.get(this)
            } else {
                if ((p.type.classifier as KClass<*>).jvmName == "java.lang.String") {

                    fields.sortedBy {
                        it.findAnnotation<CombineTo>()!!.index
                    }.fold("") { acc, prop ->
                        if (acc.isNotEmpty())
                            "$acc${prop.findAnnotation<CombineTo>()!!.separator}${fieldsValues[prop.name]}"
                        else
                            "${fieldsValues[prop.name]}"
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
        fieldsAnnotations.clear()
        fieldsValues.clear()
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


