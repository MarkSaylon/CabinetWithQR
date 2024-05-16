import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.util.Log

class CabinetDao(private val context: Context) {
    private val dbHelper = DBHelper(context)

    fun insert(time: String, state: String, cabinetName: String, notif:String, imageLink: String?) {
        val dbHelper = DBHelper(context)
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(DBHelper.COLUMN_TIME, time)
            put(DBHelper.COLUMN_STATE, state)
            put(DBHelper.COLUMN_CABINET_NAME, cabinetName)
            put(DBHelper.COLUMN_NOTIF, notif)
            if (imageLink != null) {
                put(DBHelper.COLUMN_IMAGE_LINK, imageLink)
            }
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
    //pabayaan niyo na errors here hehe di ko gets bat ayaw nila mawala pero it works fine. edit: natanggal ko na, kailangan lang pala ng checker
    fun getAllLogs(): List<CabinetLog> {
        val logList = mutableListOf<CabinetLog>()
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM ${DBHelper.TABLE_NAME}", null)
        if (cursor.moveToFirst()) {
            do {
                val idIndex = cursor.getColumnIndex(DBHelper.COLUMN_ID)
                val timeIndex = cursor.getColumnIndex(DBHelper.COLUMN_TIME)
                val stateIndex = cursor.getColumnIndex(DBHelper.COLUMN_STATE)
                val cabinetNameIndex = cursor.getColumnIndex(DBHelper.COLUMN_CABINET_NAME)
                val notifIndex = cursor.getColumnIndex(DBHelper.COLUMN_NOTIF)
                val imageLinkIndex = cursor.getColumnIndex(DBHelper.COLUMN_IMAGE_LINK)

                val log = CabinetLog(
                    if (idIndex != -1) cursor.getInt(idIndex) else -1,
                    if (timeIndex != -1) cursor.getString(timeIndex) else "",
                    if (stateIndex != -1) cursor.getString(stateIndex) else "",
                    if (cabinetNameIndex != -1) cursor.getString(cabinetNameIndex) else "",
                    if (notifIndex != -1) cursor.getString(notifIndex) else "",
                    if (imageLinkIndex != -1) cursor.getString(imageLinkIndex) else null
                )
                logList.add(log)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return logList
    }
    //dito sa retrieval di ko na gets ung error, naandar naman and I have yet to see what problems this might bring so ehhhh
    fun getLatestStateForCabinet(cabinetName: String): String? {
        val db = dbHelper.readableDatabase
        val query = "SELECT ${ DBHelper.COLUMN_STATE } FROM ${ DBHelper.TABLE_NAME } WHERE ${ DBHelper.COLUMN_CABINET_NAME } = ? ORDER BY ${ DBHelper.COLUMN_ID } DESC LIMIT 1"
        val cursor: Cursor = db.rawQuery(query, arrayOf(cabinetName))
        var latestState: String? = null

        if (cursor.moveToFirst()) {
            latestState = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_STATE))
        }

        cursor.close()
        db.close()
        return latestState
    }

    // Di niyo naman ata need ng delete so okay na to.
}

data class CabinetLog(
    val id: Int,
    val time: String,
    val state: String,
    val cabinetName: String,
    val notif :String,
    val imageLink: String?
)