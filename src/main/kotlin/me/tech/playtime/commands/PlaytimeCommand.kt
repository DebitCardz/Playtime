package me.tech.playtime.commands

import com.github.shynixn.mccoroutine.SuspendingCommandExecutor
import me.tech.chestuiplus.*
import me.tech.playtime.Playtime
import me.tech.playtime.datastore.models.PlaytimePlayer
import me.tech.playtime.datastore.models.ServerPlaytime
import me.tech.playtime.utils.toHumanReadable
import me.tech.utilities.commands.CommandInfo
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*
import kotlin.math.roundToInt

@CommandInfo(
	command = "playtime",
	description = "Playtime command."
)
class PlaytimeCommand(
	private val plugin: Playtime
): SuspendingCommandExecutor {
//	private val guiConfig = plugin
//		.configManager.get("guis/main_gui")
//		?: throw NullPointerException("Configuration guis/main_gui.yml doesn't exist")

	private val messages = plugin.messages

	private val playerManager = plugin.playerManager

	override suspend fun onCommand(
		sender: CommandSender,
		command: Command,
		label: String,
		args: Array<out String>,
	): Boolean {
		if(!sender.hasPermission("playtime.commands.playtime")) {
			messages.send(sender, "core.invalid_permissions")
			return true
		}

		val target: OfflinePlayer? =
			if(args.isEmpty()) {
				if(sender is Player) sender
				else null
			} else {
				plugin.server.getOfflinePlayer(args[0])
			}

		if(target == null) {
			messages.send(sender, "commands.playtime.invalid_player", listOf(
				Pair("target", args.getOrElse(0) { "Undefined" })
			))
			return true
		}

		val playtimePlayer = playerManager.getPossiblyUncachedPlayer(target.uniqueId).also {
			if(it != null) playerManager.updateLocalPlaytime(target.uniqueId)
		}

		if(playtimePlayer == null) {
			messages.send(sender, "commands.playtime.invalid_player", listOf(
				Pair("target", args[0])
			))
			return true
		}

		// Console sender.
		if(sender !is Player) {
			val serv = playtimePlayer.servers.firstOrNull { it.serverId.equals(plugin.serverId, true) } ?: return true
			messages.getColoredList("commands.playtime.console_message", listOf(
				Pair("player", target.name ?: "Undefined"),
				Pair("total_playtime", playtimePlayer.totalTime.toHumanReadable()),
				Pair("server", serv.serverId),
				Pair("playtime", serv.playtime.toHumanReadable())
			)).forEach(sender::sendMessage)

			return true
		}

		sender.openGUI(PlaytimeGUI(
			target,
			playtimePlayer,
			sender.uniqueId == target.uniqueId
		).render())

		return true
	}

	private inner class PlaytimeGUI(
		private val target: OfflinePlayer,
		private val playtimePlayer: PlaytimePlayer,
		private val renderSelf: Boolean
	) {
		private val playerName get() = target.name ?: "Undefined"
		private val amountOfServersStr get() = playtimePlayer.servers.size.toString()
		private val serversWord get() = if (playtimePlayer.servers.size == 1) "server" else "servers"
		private val renderType get() = if(renderSelf) "self" else "other"

		fun render() = gui(
			plugin = plugin,
			title = messages.getColored("menus.playtime.title"),
			type = GUIType.HOPPER
		) {
			all { item = item(Material.GRAY_STAINED_GLASS_PANE) { name = Component.empty() } }

			slot(0, 0) {
				item = item(Material.STRUCTURE_VOID) {
					name = messages.getColored("menus.playtime.display.exit.title")
					onClick = { whoClicked.closeInventory() }
				}
			}

			renderTotalStatisticsItem()

			if(playtimePlayer.servers.size > 1) {
				renderOtherServersItem()
			}
		}

		private fun GUI.renderTotalStatisticsItem() = slot(2, 0) {
			item = item(Material.PLAYER_HEAD) {
				val replacements = listOf(
					Pair("name", playerName),
					Pair("first_join", Date(playtimePlayer.firstJoin).toString()),
					Pair("total_time", playtimePlayer.totalTime.toHumanReadable())
				)

				name = messages.getColored("menus.playtime.display.total.title", replacements)
				lore = messages.getColoredList("menus.playtime.display.total.lore_$renderType", replacements)
				skullOwner = target
			}
		}

		private fun GUI.renderOtherServersItem() = slot(4, 0) {
			item = item(Material.BOOK) {
				val replacements = listOf(
					Pair("servers", amountOfServersStr),
					Pair("word", serversWord)
				)

				name = messages.getColored("menus.playtime.display.servers.title", replacements)
				lore = messages.getColoredList("menus.playtime.display.servers.lore_$renderType", replacements)

				onClick = {
					(whoClicked as? Player)?.openGUI(ServersGUI(playtimePlayer).render())
				}
			}
		}

		private inner class ServersGUI(playtimePlayer: PlaytimePlayer) {
			private val sortedServers = playtimePlayer.servers.sortedBy { it.playtime }.reversed()

			private val maxPages get() = ((sortedServers.size / 7).toDouble().roundToInt() + 1).run {
				return@run if(sortedServers.size % 7 == 0) this - 1 else this
			}

			fun render(
				currentPage: Int = 1
			) = gui(
				plugin = plugin,
				title = messages.getColored("menus.servers.title"),
				type = GUIType.CHEST,
				rows = 2
			) {
				fill(0, 1, 8, 1) { item = item(Material.GRAY_STAINED_GLASS_PANE) { name = Component.empty() } }

				slot(4, 1) {
					item = item(Material.STRUCTURE_VOID) {
						name = messages.getColored("menus.servers.display.exit.title")
						onClick = { whoClicked.closeInventory() }
					}
				}

				// Fill the first 6 slots of the GUI with the servers
				// we need, other 2 on the sides are reserved for page buttons.
				(6 * (currentPage - 1)).until(sortedServers.size).forEachIndexed { x, i ->
					try {
						renderServer(sortedServers[i], x + 1)
						if(x == 6) return@forEachIndexed
					} catch(_: ArrayIndexOutOfBoundsException) { return@forEachIndexed }
				}

				renderPageButtons(maxPages, currentPage)
			}

			private fun GUI.renderServer(
				server: ServerPlaytime,
				x: Int
			) = slot(x, 0) {
				val replacements = listOf(
					Pair("playtime", server.playtime.toHumanReadable()),
					Pair("server", server.serverId)
				)

				// TODO: 5/16/2022 Custom item types per server?
				item = item(Material.PAPER) {
					name = messages.getColored("menus.servers.display.server.title", replacements)
					lore = messages.getColoredList("menus.servers.display.server.lore_$renderType", replacements)
				}
			}

			private fun GUI.renderPageButtons(maxPages: Int, currentPage: Int) {
				item(Material.BLACK_STAINED_GLASS_PANE) { name = Component.empty() }.run {
					slot(0, 0) { item = this@run }
					slot(8, 0) { item = this@run }
				}

				// They're able to go back a page.
				if(currentPage != 1) {
					slot(0, 0) {
						item = item(Material.ARROW) {
							name = messages.getColored("menus.servers.display.previous.title", listOf(
								Pair("previous_page", (currentPage - 1).toString())
							))
							onClick = {
								render(currentPage - 1)
							}
						}
					}
				}

				// They're able to go to the next page.
				if(currentPage != maxPages) {
					slot(8, 0) {
						item = item(Material.ARROW) {
							name = messages.getColored("menus.servers.display.next.title", listOf(
								Pair("next_page", (currentPage + 1).toString())
							))
							onClick = {
								render(currentPage + 1)
							}
						}
					}
				}
			}
		}
	}
}