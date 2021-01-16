package mapper

typealias ConditionalIgnore<T> = (src: T) -> Boolean

class ConfigMapper<T : Any, R : Any> {

    private val listIgnoredAtt: MutableList<String>
        get() = emptyList<String>().toMutableList()

    internal val listIgnoredExpression: MutableList<ConditionalIgnore<T>>
        get() = emptyList<ConditionalIgnore<T>>().toMutableList()

    fun ignoreAtt(srcAtt: String): ConfigMapper<T, R> {
        if (!listIgnoredAtt.contains(srcAtt))
            listIgnoredAtt.add(srcAtt)

        return this
    }

    fun ignoreIf(expression: ConditionalIgnore<T>): ConfigMapper<T, R> {

        listIgnoredExpression.add(expression)

        return this
    }
}
