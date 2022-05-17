package me.tech.playtime.utils

import me.tech.playtime.datastore.models.PlaytimePlayer
import java.util.*

internal operator fun Set<PlaytimePlayer>.get(uuid: UUID): PlaytimePlayer? =
	this.firstOrNull { it.uuid == uuid }