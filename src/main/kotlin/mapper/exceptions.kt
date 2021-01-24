package mapper

abstract class MapperException(error: String) : Exception(error)

object UnSupportedMappingType : MapperException("for now,we only support data class")
object UndefinedDestinationObject : MapperException("Undefined destination object,you cannot map to null object")
class UnSupportedMultipleExpression(operation: String) :
    MapperException("Unsupported multiple $operation for the same field")
