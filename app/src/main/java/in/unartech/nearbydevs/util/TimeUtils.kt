package `in`.unartech.nearbydevs.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object TimeUtils {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun formatTimestamp(millis: Long): String {
        return dateFormat.format(Date(millis))
    }

    fun formatRelativeTime(millis: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - millis

        return when {
            diff < TimeUnit.SECONDS.toMillis(5) -> "just now"
            diff < TimeUnit.MINUTES.toMillis(1) -> "${TimeUnit.MILLISECONDS.toSeconds(diff)}s ago"
            diff < TimeUnit.HOURS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toMinutes(diff)}m ago"
            diff < TimeUnit.DAYS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toHours(diff)}h ago"
            diff < TimeUnit.DAYS.toMillis(7) -> "${TimeUnit.MILLISECONDS.toDays(diff)}d ago"
            else -> formatTimestamp(millis)
        }
    }
}
