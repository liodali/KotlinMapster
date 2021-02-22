package mapper

typealias ConditionalIgnore<T> = (src: T) -> Boolean
typealias TransformationExpression<T> = (src: T) -> Any

/*
* ConfigMapper
*
*  give you possible to configure mapper by ignore field or ignore field in specific condition
*  or transform value of specific field
*
*

*/
class ConfigMapper<T : Any, R : Any> {

    internal var listIgnoredAttribute: List<String> = emptyList()

    internal var listMappedAttributes: List<Pair<Array<String>, String>> =
        emptyList()

    internal var listIgnoredExpression: List<Pair<String, ConditionalIgnore<T>>> =
        emptyList()

    internal var listTransformationExpression: List<Pair<String, TransformationExpression<T>>> =
        emptyList()

    internal var listNestedTransformationExpression: List<Pair<String, TransformationExpression<*>>> =
        emptyList()

    internal var listInverseTransformationExpression: List<Pair<String, TransformationExpression<*>>> =
        emptyList()

    fun hasConfiguration(): Boolean = this.listIgnoredExpression.isNotEmpty() ||
        this.listIgnoredAttribute.isNotEmpty() || this.listTransformationExpression.isNotEmpty() || this.listNestedTransformationExpression.isNotEmpty() || this.listMappedAttributes.isNotEmpty()

    fun ignoreAtt(srcAtt: String): ConfigMapper<T, R> {
        if (!listIgnoredAttribute.contains(srcAtt)) {
            val mutableList = this.listIgnoredAttribute.toMutableList()
            mutableList.add(srcAtt)
            this.listIgnoredAttribute = mutableList.toList()
        }
        return this
    }

    fun ignoreIf(
        srcAttribute: String,
        expression: ConditionalIgnore<T>
    ): ConfigMapper<T, R> {
        val index = listIgnoredExpression.indexOfFirst {
            it.first == srcAttribute
        }
        val mutableList = this.listIgnoredExpression.toMutableList()
        when (index) {
            -1 -> {
                mutableList.add(Pair(srcAttribute, expression))
            }
            else -> {
                val occSrcAttIgnoreIf = listIgnoredExpression.takeWhile {
                    it.first == srcAttribute
                }.size
                if (occSrcAttIgnoreIf != 1)
                    throw UnSupportedMultipleExpression("ignoreIf")

                mutableList[index] = Pair(srcAttribute, expression)
            }
        }

        this.listIgnoredExpression = mutableList.toList()
        return this
    }

    fun transformation(
        attribute: String,
        expression: TransformationExpression<T>
    ): ConfigMapper<T, R> {
        if (listIgnoredAttribute.contains(attribute) || listIgnoredExpression.firstOrNull {
            it.first == attribute
        } != null
        ) {
            println("Unnecessary transformation for ignore field")
            return this
        }

        val index = listTransformationExpression.indexOfFirst {
            it.first == attribute
        }
        val mutableList = this.listTransformationExpression.toMutableList()
        when (index) {
            -1 -> mutableList.add(Pair(attribute, expression))
            else -> {
                val occTransformation = listTransformationExpression.takeWhile {
                    it.first == attribute
                }.size
                if (occTransformation != 1) {
                    throw UnSupportedMultipleExpression("transformation")
                }
                mutableList[index] = Pair(attribute, expression)
            }
        }

        this.listTransformationExpression = mutableList.toMutableList()
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
        val mutableList = this.listNestedTransformationExpression.toMutableList()
        when (index) {
            -1 -> mutableList.add(Pair(srcAttribute, expression as TransformationExpression<*>))
            else -> {
                val occTransformation = listNestedTransformationExpression.takeWhile {
                    it.first == srcAttribute
                }.size
                if (occTransformation != 1) {
                    throw UnSupportedMultipleExpression("nested transformation")
                }
                mutableList[index] =
                    Pair(srcAttribute, expression as TransformationExpression<*>)
            }
        }

        this.listNestedTransformationExpression = mutableList.toList()
        return this
    }

    fun inverseTransformation(
        attribute: String,
        expression: TransformationExpression<R>
    ): ConfigMapper<T, R> {
        if (listIgnoredAttribute.contains(attribute) || listIgnoredExpression.firstOrNull {
            it.first == attribute
        } != null
        ) {
            println("Unnecessary transformation for ignore field")
            return this
        }

        val index = listInverseTransformationExpression.indexOfFirst {
            it.first == attribute
        }
        val mutableList = this.listInverseTransformationExpression.toMutableList()
        when (index) {
            -1 -> mutableList.add(Pair(attribute, expression as TransformationExpression<*>))
            else -> {
                val occTransformation = listInverseTransformationExpression.takeWhile {
                    it.first == attribute
                }.size
                if (occTransformation != 1) {
                    throw UnSupportedMultipleExpression("nested transformation")
                }
                mutableList[index] =
                    Pair(attribute, expression as TransformationExpression<*>)
            }
        }

        this.listInverseTransformationExpression = mutableList.toList()
        return this
    }

    fun map(
        from: Array<String>,
        to: String
    ): ConfigMapper<T, R> {
        if (listMappedAttributes.firstOrNull {
            it.first.toMutableList().containsAll(from.toMutableList())
        } != null
        ) {
            throw IllegalArgumentException("cannot map $from to multiple destination field")
        }
        val mutableList = listMappedAttributes.toMutableList()
        mutableList.add(Pair(from, to))
        this.listMappedAttributes = mutableList.toList()
        return this
    }
}
