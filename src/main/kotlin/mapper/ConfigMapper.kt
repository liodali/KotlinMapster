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

    fun hasConfiguration(): Boolean = this.listIgnoredExpression.isNotEmpty() ||
            this.listIgnoredAttribute.isNotEmpty() || this.listTransformationExpression.isNotEmpty() || this.listMappedAttributes.isNotEmpty()

    fun ignoreAtt(srcAtt: String): ConfigMapper<T, R> {
        if (!listIgnoredAttribute.contains(srcAtt))
            this.listIgnoredAttribute.add(srcAtt)
        return this
    }

    fun ignoreIf(srcAttribute: String, expression: ConditionalIgnore<T>): ConfigMapper<T, R> {
        val pairExist = listIgnoredExpression.firstOrNull {
            it.first == srcAttribute
        }
        if (pairExist == null)
            listIgnoredExpression.add(Pair(srcAttribute, expression))
        return this
    }

    fun transformation(srcAttribute: String, expression: TransformationExpression<T>): ConfigMapper<T, R> {
        if (listIgnoredAttribute.contains(srcAttribute) || listIgnoredExpression.firstOrNull {
                it.first == srcAttribute
            } != null) {
            println("Unnecessary transformation for ignore field")
            return this
        }
        val pairExist = listTransformationExpression.firstOrNull {
            it.first == srcAttribute
        }
        if (pairExist == null)
            listTransformationExpression.add(Pair(srcAttribute, expression))
        return this
    }

    fun map(srcAttribute: String, destAttribute: String): ConfigMapper<T, R> {
        if (listMappedAttributes.firstOrNull {
                it.first == srcAttribute
            } != null) {
            throw IllegalArgumentException("cannot map $srcAttribute to multiple destination field")
        }

        listMappedAttributes.add(Pair(srcAttribute, destAttribute))
        return this
    }
}
