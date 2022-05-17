package me.tech.playtime.datastore.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ServerPlaytime(
	@SerialName("server_id")
	val serverId: String,
	var playtime: Long
)