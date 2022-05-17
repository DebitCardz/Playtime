package me.tech.playtime.utils

import java.util.concurrent.TimeUnit

/**
 * Convert a long into a human readable string.
 * @return Human readable date.
 */
internal fun Long.toHumanReadable(): String {
	val total = TimeUnit.MILLISECONDS.toSeconds(this)
	val day = TimeUnit.SECONDS.toDays(total).toInt()
	val hours = TimeUnit.SECONDS.toHours(total) - day * 24
	val minute = TimeUnit.SECONDS.toMinutes(total) - TimeUnit.SECONDS.toHours(total) * 60
	val second = TimeUnit.SECONDS.toSeconds(total) - TimeUnit.SECONDS.toMinutes(total) * 60

	// TODO: 5/14/2022 Customize messages. 
	
	val builder = StringBuilder()
	// day.
	if(total >= 86400) builder.append("&f${day}d&7, ")
	// hour.
	if(total >= 3600) builder.append("&f${hours}h&7, ")
	// minute.
	if(total >= 60) builder.append("&f${minute}m&7,")
	if(builder.isNotEmpty()) builder.append(" ")
	builder.append("&f${second}s&7")

	return builder.toString()
}