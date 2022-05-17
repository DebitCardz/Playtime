package me.tech.playtime.datastore.repo

import me.tech.playtime.datastore.models.PlaytimePlayer
import org.bukkit.entity.Player
import java.util.*

interface PlayerRepo {
	/**
	 * Get a player model.
	 * @param uuid
	 * @return Player model.
	 */
	suspend fun get(uuid: UUID): PlaytimePlayer?

	/**
	 * Create a new player model in the datastore.
	 * @param player
	 */
	suspend fun create(player: PlaytimePlayer): PlaytimePlayer

	/**
	 * Update a player's playtime in the datastore.
	 * @param player
	 */
	suspend fun updatePlaytime(player: PlaytimePlayer)
}