package me.tech.playtime.datastore.impl

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import me.tech.playtime.datastore.DataStore
import me.tech.playtime.datastore.repo.impl.MongoPlayerRepo
import org.bson.UuidRepresentation
import org.bukkit.configuration.ConfigurationSection
import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import javax.naming.ConfigurationException

class MongoDataStore(
	client: CoroutineClient,
	dbName: String
): DataStore {
	private val db = client.getDatabase(dbName)

	override val playerRepo = MongoPlayerRepo(db)

	companion object {
		// We have to set the new mappings or else
		// an error will be thrown.
		init {
			System.setProperty(
				"org.litote.mongo.test.mapping.service",
				"org.litote.kmongo.serialization.SerializationClassMappingTypeService"
			)
		}

		fun create(database: ConfigurationSection): MongoDataStore {
			return MongoDataStore(
				KMongo.createClient(
					MongoClientSettings
						.builder()
						.applyConnectionString(
							ConnectionString(
								database.getString("connection_string") ?: throw ConfigurationException("Missing connection_string in config.")
							)
						)
						.uuidRepresentation(UuidRepresentation.STANDARD)
						.build()
				).coroutine,
				database.getString("database") ?: "playtime"
			)
		}
	}
}