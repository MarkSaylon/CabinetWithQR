import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.util.Log

class CabinetDao(private val context: Context) {
    private val dbHelper = DBHelper(context)

    fun insert(time: String, state: String, cabinetName: String, notif:String) {
        val dbHelper = DBHelper(context)
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(DBHelper.COLUMN_TIME, time)
            put(DBHelper.COLUMN_STATE, state)
            put(DBHelper.COLUMN_CABINET_NAME, cabinetName)
            put(DBHelper.COLUMN_NOTIF, notif)
        }

        val result = db.insert(DBHelper.TABLE_NAME, null, values)
        if (result == -1L) {
            Log.e("CabinetDao", "Error inserting data into database")
        } else {
            Log.d("CabinetDao", "Data inserted successfully with row ID: $result")
        }

        db.close()
        dbHelper.close()
    }

    fun getAllLogs(): List<CabinetLog> {
        val logList = mutableListOf<CabinetLog>()
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM ${DBHelper.TABLE_NAME}", null)
        if (cursor.moveToFirst()) {
            do {
                val log = CabinetLog(
                    cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TIME)),
                    cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_STATE)),
                    cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_CABINET_NAME)),
                    cursor.getString((cursor.getColumnIndex((DBHelper.COLUMN_NOTIF))))
                )
                logList.add(log)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return logList
    }

    // Add methods for updating and deleting data if needed
}

data class CabinetLog(
    val id: Int,
    val time: String,
    val state: String,
    val cabinetName: String,
    val notif :String
)