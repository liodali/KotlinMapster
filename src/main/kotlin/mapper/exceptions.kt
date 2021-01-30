package mapper

internal abstract class MapperException(error: String) : Exception(error)

internal object UnSupportedMappingType : MapperException("for now,we only support data class")
internal object UndefinedDestinationObject :
    MapperException("Undefined destination object,you cannot map to null object")
internal object UndefinedSourceObject :
    MapperException("Undefined source object,you cannot map from null object")

internal class UnSupportedMultipleExpression(operation: String) :
    MapperException("Unsupported multiple $operation for the same field")
