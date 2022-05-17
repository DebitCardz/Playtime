package me.tech.playtime.datastore

import me.tech.playtime.datastore.repo.PlayerRepo

interface DataStore {
	val playerRepo: PlayerRepo
}