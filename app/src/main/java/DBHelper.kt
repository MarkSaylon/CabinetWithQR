import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "cabinet_db"
        private const val DATABASE_VERSION = 1
        const val TABLE_NAME = "cabinet_log"
        const val COLUMN_ID = "id"
        const val COLUMN_TIME = "time"
        const val COLUMN_STATE = "state"
        const val COLUMN_CABINET_NAME = "cabinet_name"
        const val COLUMN_NOTIF = "notif"
        const val COLUMN_IMAGE_LINK = "imageLink"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = "CREATE TABLE $TABLE_NAME ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_TIME TEXT, $COLUMN_STATE TEXT, $COLUMN_CABINET_NAME TEXT, $COLUMN_NOTIF, $COLUMN_IMAGE_LINK)"
        db.execSQL(createTableQuery)
        Log.d("DBHelper", "Table created: $TABLE_NAME")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
        Log.d("DBHelper", "Table upgraded: $TABLE_NAME")
    }
}