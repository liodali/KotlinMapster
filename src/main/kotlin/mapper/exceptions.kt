package mapper

abstract class MapperException(error: String) : Exception(error)

object UnSupportedMappingType : MapperException("for now,we only support data class")
