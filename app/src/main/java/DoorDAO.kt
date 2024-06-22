import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.util.Log

class DoorDAO(private val context: Context) {
    private val dbHelper = DoorDBHelper(context)

    fun insert(time: String, doorState: String) {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(DoorDBHelper.COLUMN_TIME, time)
            put(DoorDBHelper.COLUMN_DOOR_STATE, doorState)
        }

        val result = db.insert(DoorDBHelper.TABLE_NAME, null, values)
        if (result == -1L) {
            Log.e("DoorDAO", "Error inserting data into database")
        } else {
            Log.d("DoorDAO", "Data inserted successfully with row ID: $result")
        }

        db.close()
    }

    fun getAllLogs(): List<DoorLog> {
        val logList = mutableListOf<DoorLog>()
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM ${DoorDBHelper.TABLE_NAME}", null)
        if (cursor.moveToFirst()) {
            do {
                val idIndex = cursor.getColumnIndex(DoorDBHelper.COLUMN_ID)
                val timeIndex = cursor.getColumnIndex(DoorDBHelper.COLUMN_TIME)
                val doorStateIndex = cursor.getColumnIndex(DoorDBHelper.COLUMN_DOOR_STATE)

                val log = DoorLog(
                    if (idIndex != -1) cursor.getInt(idIndex) else -1,
                    if (timeIndex != -1) cursor.getString(timeIndex) else "",
                    if (doorStateIndex != -1) cursor.getString(doorStateIndex) else ""
                )
                logList.add(log)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return logList
    }

    fun getLatestDoorState(): String {
        val db = dbHelper.readableDatabase
        val query = "SELECT ${DoorDBHelper.COLUMN_DOOR_STATE} FROM ${DoorDBHelper.TABLE_NAME} ORDER BY ${DoorDBHelper.COLUMN_ID} DESC LIMIT 1"
        val cursor: Cursor = db.rawQuery(query, null)
        var latestDoorState = "closed" // Default value

        if (cursor.moveToFirst()) {
            // Check if the column index is valid (not -1)
            val columnIndex = cursor.getColumnIndex(DoorDBHelper.COLUMN_DOOR_STATE)
            if (columnIndex != -1) {
                latestDoorState = cursor.getString(columnIndex)
            } else {
                // Throw an exception if column index is -1
                cursor.close()
                db.close()
                throw IllegalStateException("Column ${DoorDBHelper.COLUMN_DOOR_STATE} does not exist")
            }
        }

        cursor.close()
        db.close()
        return latestDoorState
    }
}

data class DoorLog(
    val id: Int,
    val time: String,
    val doorState: String
)