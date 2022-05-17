package me.tech.playtime

import me.tech.playtime.datastore.models.PlaytimePlayer
import me.tech.playtime.utils.get
import org.bukkit.entity.Player
import java.util.*
import kotlin.NoSuchElementException

class PlayerManager(plugin: Playtime) {
	private val _players = mutableSetOf<PlaytimePlayer>()
	val players: Set<PlaytimePlayer>
		get() = _players

	private val playerRepo = plugin.datastore.playerRepo

	private val serverId = plugin.serverId

	fun add(player: PlaytimePlayer) {
		_players.add(player)
	}

	fun remove(uuid: UUID) {
		_players.removeIf {
			it.uuid == uuid
		}
	}

	suspend fun getPossiblyUncachedPlayer(uuid: UUID): PlaytimePlayer? {
		val player = players[uuid].run {
			if(this != null) {
				// Get their up-to-date playtime before returning.
				updateLocalPlaytime(uuid)
				return@run this
			}

			// Retrieve them from the datastore.
			return@run playerRepo.get(uuid)
		}

		return player
	}

	/**
	 * Update the currently cached playtime before pushing
	 * it to the datastore.
	 */
	fun updateLocalPlaytime(uuid: UUID) {
		val player = players[uuid] ?: return

		try {
			player.servers.first {
				it.serverId.equals(serverId, true)
			}.playtime += System.currentTimeMillis() - player.lastUpdate
		} catch(ex: NoSuchElementException) {
			ex.printStackTrace()
		}

		player.lastUpdate = System.currentTimeMillis()
	}

	suspend fun save(uuid: UUID) {
		val player = players[uuid] ?: return

		updateLocalPlaytime(uuid)
		playerRepo.updatePlaytime(player)
	}

	suspend fun saveAndRemove(uuid: UUID) {
		save(uuid).also {
			remove(uuid)
		}
	}
}