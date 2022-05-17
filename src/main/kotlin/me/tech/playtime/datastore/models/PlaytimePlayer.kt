package me.tech.playtime.datastore.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.github.jershell.kbson.UUIDSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Transient
import java.util.*

@JsonIgnoreProperties(value = ["lastUpdate", "totalTime"])
@Serializable
data class PlaytimePlayer(
	@Serializable(with = UUIDSerializer::class)
	// there's probably a better way to do this lol
	@SerialName("_id")
	val uuid: UUID,
	@SerialName("first_join")
	val firstJoin: Long,
	val servers: MutableList<ServerPlaytime>
) {
	@Transient
	var lastUpdate = System.currentTimeMillis()

	val totalTime get() = servers.sumOf { it.playtime }
}