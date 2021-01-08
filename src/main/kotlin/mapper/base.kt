package mapper

import kotlin.reflect.*
import kotlin.reflect.full.*

fun <T : Any, R : Any> T.adaptTo(dest: KClass<R>): R {
    if (dest.isData) {
        //argDest.map { it.key to it.value }.toTypedArray()
        val argsValues = dest.primaryConstructor!!.parameters.map { p ->
            val field = (this::class as KClass<Any>).declaredMemberProperties.first { p.name == it.name }
            var v = field.get(this)
            if ((p.type.classifier as KClass<*>).isData) {
                v = v!!.adaptTo(
                    dest.memberProperties
                        .find { it.name == field.name }!!
                        .returnType.classifier as KClass<*>
                )
            } else {
                if ((p.type.classifier as KClass<*>).superclasses.contains(Collection::class)) {

                    val typeDest = (dest.memberProperties.first { td -> td.name == p.name }
                        .returnType).arguments.first().type!!
                  v = (v!! as Collection<Any>).adaptListTo(typeDest.classifier as KClass<*> )
                }
            }
            v
        }.toTypedArray()
        return dest.primaryConstructor!!.call(*argsValues)
    }
    return dest.createInstance()

}

fun <T : Any, R : Any> Collection<T>.adaptListTo(dest: KClass<R>): Collection<R> {
    val listDest = emptyList<R>().toMutableList()
    this.forEach {
        listDest.add(it.adaptTo(dest))
    }

    return listDest

}

