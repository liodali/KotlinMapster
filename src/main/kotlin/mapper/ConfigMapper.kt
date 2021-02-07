package mapper

typealias ConditionalIgnore<T> = (src: T) -> Boolean
typealias TransformationExpression<T> = (src: T) -> Any

/*
* ConfigMapper
*
*  give you possible to configure mapper by ignore field or ignore field in specific condition
* or transform value of specific field
*
*
*
*
*
*
 */
class ConfigMapper<T : Any, R : Any> {

    internal val listIgnoredAttribute: MutableList<String> = emptyList<String>().toMutableList()
    internal val listMappedAttributes: MutableList<Pair<String, String>> =
        emptyList<Pair<String, String>>().toMutableList()
    internal val listIgnoredExpression: MutableList<Pair<String, ConditionalIgnore<T>>> =
        emptyList<Pair<String, ConditionalIgnore<T>>>().toMutableList()
    internal val listTransformationExpression: MutableList<Pair<String, TransformationExpression<T>>> =
        emptyList<Pair<String, TransformationExpression<T>>>().toMutableList()

    internal val listNestedTransformationExpression: MutableList<Pair<String, TransformationExpression<*>>> =
        emptyList<Pair<String, TransformationExpression<*>>>().toMutableList()

    fun hasConfiguration(): Boolean = this.listIgnoredExpression.isNotEmpty() ||
        this.listIgnoredAttribute.isNotEmpty() || this.listTransformationExpression.isNotEmpty() || this.listNestedTransformationExpression.isNotEmpty() || this.listMappedAttributes.isNotEmpty()

    fun ignoreAtt(srcAtt: String): ConfigMapper<T, R> {
        if (!listIgnoredAttribute.contains(srcAtt))
            this.listIgnoredAttribute.add(srcAtt)
        return this
    }

    fun ignoreIf(srcAttribute: String, expression: ConditionalIgnore<T>): ConfigMapper<T, R> {
        val index = listIgnoredExpression.indexOfFirst {
            it.first == srcAttribute
        }
        if (index == -1)
            listIgnoredExpression.add(Pair(srcAttribute, expression))
        else {
            val occSrcAttIgnoreIf = listIgnoredExpression.takeWhile {
                it.first == srcAttribute
            }.size
            if (occSrcAttIgnoreIf == 1)
                listIgnoredExpression[index] = Pair(srcAttribute, expression)
            else {
                throw UnSupportedMultipleExpression("ignoreIf")
            }
        }
        return this
    }

    fun transformation(srcAttribute: String, expression: TransformationExpression<T>): ConfigMapper<T, R> {
        if (listIgnoredAttribute.contains(srcAttribute) || listIgnoredExpression.firstOrNull {
            it.first == srcAttribute
        } != null
        ) {
            println("Unnecessary transformation for ignore field")
            return this
        }

        val index = listTransformationExpression.indexOfFirst {
            it.first == srcAttribute
        }
        if (index == -1)
            listTransformationExpression.add(Pair(srcAttribute, expression))
        else {
            val occTransformation = listTransformationExpression.takeWhile {
                it.first == srcAttribute
            }.size
            if (occTransformation == 1) {
                listTransformationExpression[index] = Pair(srcAttribute, expression)
            } else {
                throw UnSupportedMultipleExpression("transformation")
            }
        }
        return this
    }

    fun <K : Any> nestedTransformation(
        srcAttribute: String,
        expression: TransformationExpression<K>
    ): ConfigMapper<T, R> {
        if (listIgnoredAttribute.contains(srcAttribute) || listIgnoredExpression.firstOrNull {
            it.first == srcAttribute
        } != null
        ) {
            println("Unnecessary transformation for ignore field")
            return this
        }

        val index = listNestedTransformationExpression.indexOfFirst {
            it.first == srcAttribute
        }
        if (index == -1)
            listNestedTransformationExpression.add(Pair(srcAttribute, expression as TransformationExpression<*>))
        else {
            val occTransformation = listNestedTransformationExpression.takeWhile {
                it.first == srcAttribute
            }.size
            if (occTransformation == 1) {
                listNestedTransformationExpression[index] =
                    Pair(srcAttribute, expression as TransformationExpression<*>)
            } else {
                throw UnSupportedMultipleExpression("transformation")
            }
        }
        return this
    }

    fun map(srcAttribute: String, destAttribute: String): ConfigMapper<T, R> {
        if (listMappedAttributes.firstOrNull {
            it.first == srcAttribute
        } != null
        ) {
            throw IllegalArgumentException("cannot map $srcAttribute to multiple destination field")
        }

        listMappedAttributes.add(Pair(srcAttribute, destAttribute))
        return this
    }
}
