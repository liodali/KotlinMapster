package mapper

import java.lang.IllegalStateException
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.superclasses
import kotlin.reflect.jvm.jvmName
import mapper.annotations.CombineTo
import mapper.annotations.MapTo

fun <T : Any, R : Any> T.adaptTo(dest: KClass<R>): R {
    if (dest.isData) {
        val argsValues = dest.primaryConstructor!!.parameters.map { p ->
            val fields = (this::class as KClass<Any>).declaredMemberProperties.filter { prop ->
                val annotation = prop.findAnnotation<CombineTo>()
                annotation?.destAtt?.equals(p.name) ?: false
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
                            "$acc${prop.findAnnotation<CombineTo>()!!.separator}${prop.get(this)}"
                        else
                            "${prop.get(this)}"
                    }
                } else {
                    throw IllegalStateException("Annotation CombineTo supported only with String type")
                }
            }
            if ((p.type.classifier as KClass<*>).isData) {
                v = v!!.adaptTo(
                    dest.memberProperties
                        .find { it.name == p.name }!!
                        .returnType.classifier as KClass<*>
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
