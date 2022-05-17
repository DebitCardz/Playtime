package me.tech.playtime.datastore.repo.impl

import me.tech.playtime.datastore.repo.PlayerRepo
import me.tech.playtime.datastore.models.PlaytimePlayer
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.set
import org.litote.kmongo.setTo
import java.util.*

class MongoPlayerRepo(database: CoroutineDatabase): PlayerRepo {
	private val collection = database.getCollection<PlaytimePlayer>("players")

	override suspend fun get(uuid: UUID): PlaytimePlayer? {
		return collection.findOneById(uuid)
	}

	override suspend fun create(player: PlaytimePlayer): PlaytimePlayer {
		collection.insertOne(player)
		return player
	}

	override suspend fun updatePlaytime(player: PlaytimePlayer) {
		collection.updateOneById(
			player.uuid,
			set(
				PlaytimePlayer::servers setTo player.servers
			)
		)
	}
}