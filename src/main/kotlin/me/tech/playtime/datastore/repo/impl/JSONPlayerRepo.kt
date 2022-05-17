package me.tech.playtime.datastore.repo.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.tech.playtime.datastore.repo.PlayerRepo
import me.tech.playtime.datastore.models.PlaytimePlayer
import org.bukkit.Bukkit
import java.io.File
import java.io.FileNotFoundException
import java.util.*

class JSONPlayerRepo(
	dataFolder: File,
	private val mapper: ObjectMapper
): PlayerRepo {
	private val playerDataDir = File(dataFolder, "playerdata").also {
		if(!it.exists()) { it.mkdir() }
	}

	override suspend fun get(uuid: UUID): PlaytimePlayer? {
		return try {
			mapper.readValue<PlaytimePlayer>(
				getPlayerFile(uuid).readText(Charsets.UTF_8)
			)
		} catch(_: FileNotFoundException) {
			null
		}
	}

	override suspend fun create(player: PlaytimePlayer): PlaytimePlayer {
		withContext(Dispatchers.IO) {
			mapper.writeValue(
				getPlayerFile(player.uuid).also {
					if(!it.exists()) it.createNewFile()
				},
				player
			)
		}

		return player
	}

	override suspend fun updatePlaytime(player: PlaytimePlayer) {
		withContext(Dispatchers.IO) {
			mapper.writeValue(getPlayerFile(player.uuid), player)
		}
	}

	private fun getPlayerFile(uuid: UUID): File = File(playerDataDir, "$uuid.json")
}