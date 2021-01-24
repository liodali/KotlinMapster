package mapper

abstract class MapperException(error: String) : Exception(error)

object UnSupportedMappingType : MapperException("for now,we only support data class")
class UnSupportedMultipleExpression(operation: String) :
    MapperException("Unsupported multiple $operation for the same field")
