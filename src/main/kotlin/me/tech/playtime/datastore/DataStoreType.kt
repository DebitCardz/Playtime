package me.tech.playtime.datastore

enum class DataStoreType(
	val type: String
) {
	JSON("JSON"),
	MONGO("Mongo");

	companion object {
		fun fromType(type: String): DataStoreType =
			values().firstOrNull { it.type.equals(type, true) } ?: JSON
	}

	override fun toString(): String = this.type
}