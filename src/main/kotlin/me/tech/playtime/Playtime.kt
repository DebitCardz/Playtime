package me.tech.playtime

import com.github.shynixn.mccoroutine.SuspendingJavaPlugin
import com.github.shynixn.mccoroutine.registerSuspendingEvents
import me.tech.playtime.commands.PlaytimeCommand
import me.tech.playtime.datastore.DataStore
import me.tech.playtime.datastore.DataStoreType
import me.tech.playtime.datastore.impl.JSONDataStore
import me.tech.playtime.datastore.models.PlaytimePlayer
import me.tech.playtime.datastore.models.ServerPlaytime
import me.tech.playtime.datastore.impl.MongoDataStore
import me.tech.utilities.ConfigManager
import me.tech.utilities.MessageContainer
import me.tech.utilities.commands.CommandManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.lang.RuntimeException
import javax.naming.ConfigurationException

class Playtime: SuspendingJavaPlugin() {
	val configManager = ConfigManager(this).also {
		it.load("config.yml")
	}

	val messages = MessageContainer(this, "messages.yml")

	lateinit var datastore: DataStore

	lateinit var playerManager: PlayerManager

	val serverId = configManager
		.get("config")
		?.getString("server.name")
		?: throw ConfigurationException("Section server.name doesn't exist in config.yml")

	override suspend fun onEnableAsync() {
		loadDataStore()

		playerManager = PlayerManager(this)

		CommandManager.registerCommands(
			this,
			setOf(PlaytimeCommand(this))
		)

		server.pluginManager.registerSuspendingEvents(object: Listener {
			private val playerRepo = datastore.playerRepo

			@EventHandler
			suspend fun onPlayerJoin(ev: PlayerJoinEvent) {
				val uuid = ev.player.uniqueId

				// If they're in the datastore then return their stats,
				// if not create a new document for them and carry on.
				val playtimePlayer = playerRepo.get(uuid).run {
					return@run this ?: playerRepo.create(PlaytimePlayer(
						uuid = uuid,
						firstJoin = System.currentTimeMillis(),
						servers = mutableListOf(ServerPlaytime(serverId, 0L))
					))
				}

				// Add them to cache.
				playerManager.add(playtimePlayer)
			}

			@EventHandler
			suspend fun onPlayerQuit(ev: PlayerQuitEvent) {
				// Save them to the datastore as they quit.
				playerManager.saveAndRemove(ev.player.uniqueId)
			}
	      }, this)
	}

	override suspend fun onDisableAsync() {
		// Make sure we save everyone's playtime.
		server.onlinePlayers.forEach {
			playerManager.saveAndRemove(it.uniqueId)
		}
	}

	private fun loadDataStore() {
		val conf = configManager.get("config") ?: throw RuntimeException("config was not initialized")

		val type = DataStoreType.fromType(
			conf.getString("datastore.type") ?: run {
				logger.warning("DataStore type was not specified in the config, defaulting to JSON.")
				return@run "json"
			}
		)

		logger.info("Using $type as the data storage method.")
		datastore = when(type) {
			DataStoreType.JSON -> JSONDataStore.create(this)
			DataStoreType.MONGO -> MongoDataStore.create(conf.getConfigurationSection("datastore") ?: throw RuntimeException("Missing configuration section datastore"))
 		}
	}
}