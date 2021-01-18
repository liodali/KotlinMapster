package mapper

typealias ConditionalIgnore<T> = (src: T) -> Boolean

class ConfigMapper<T : Any, R : Any> {

    internal val listIgnoredAtt: MutableList<String> = emptyList<String>().toMutableList()
    internal val listIgnoredExpression: MutableList<Pair<String, ConditionalIgnore<T>>> =
        emptyList<Pair<String, ConditionalIgnore<T>>>().toMutableList()

    fun ignoreAtt(srcAtt: String): ConfigMapper<T, R> {
        if (!listIgnoredAtt.contains(srcAtt))
            this.listIgnoredAtt.add(srcAtt)
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
}
