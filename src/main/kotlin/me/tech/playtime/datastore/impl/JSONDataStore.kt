package me.tech.playtime.datastore.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import me.tech.playtime.Playtime
import me.tech.playtime.datastore.DataStore
import me.tech.playtime.datastore.repo.impl.JSONPlayerRepo
import java.util.*

class JSONDataStore(
	private val plugin: Playtime
): DataStore {
	private val mapper = ObjectMapper().apply {
		registerKotlinModule()
	}

	override val playerRepo = JSONPlayerRepo(plugin.dataFolder, mapper)

	companion object {
		fun create(plugin: Playtime): JSONDataStore {
			return JSONDataStore(plugin)
		}
	}
}