package com.example.ecommerceapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class LocalAppDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "ecommerce_local.db"
        private const val DATABASE_VERSION = 1

        // User Dashboard Orders table
        private const val TABLE_ORDERS = "user_orders"
        private const val COL_ORDER_ID = "id"
        private const val COL_USER_ID = "user_id"
        private const val COL_AMOUNT = "amount"
        private const val COL_CATEGORY = "category"
        private const val COL_STATUS = "status"

        // Supplier Dashboard Metrics table
        private const val TABLE_SUPPLIER_METRICS = "supplier_metrics"
        private const val COL_SUPPLIER_ID = "supplier_id"
        private const val COL_REVENUE = "revenue"
        private const val COL_UNITS_SOLD = "units_sold"
        private const val COL_TOP_PRODUCT_NAME = "top_product_name"
        private const val COL_TOP_PRODUCT_STATS = "top_product_stats"
        private const val COL_TOP_PRODUCT_REVENUE = "top_product_revenue"

        // Admin Records table (for users, products, applications, disputes)
        private const val TABLE_ADMIN_RECORDS = "admin_records"
        private const val COL_RECORD_ID = "record_id"
        private const val COL_TYPE = "type" // "user", "product", "seller", "dispute"
        private const val COL_TITLE = "title"
        private const val COL_SUBTITLE = "subtitle"
        private const val COL_RECORD_STATUS = "record_status"
        private const val COL_ACTION_ONE = "action_one"
        private const val COL_ACTION_TWO = "action_two"
        private const val COL_ACTION_THREE = "action_three"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createOrdersTable = """
            CREATE TABLE $TABLE_ORDERS (
                $COL_ORDER_ID TEXT PRIMARY KEY,
                $COL_USER_ID TEXT,
                $COL_AMOUNT REAL,
                $COL_CATEGORY TEXT,
                $COL_STATUS TEXT
            )
        """.trimIndent()

        val createSupplierTable = """
            CREATE TABLE $TABLE_SUPPLIER_METRICS (
                $COL_SUPPLIER_ID TEXT PRIMARY KEY,
                $COL_REVENUE REAL,
                $COL_UNITS_SOLD INTEGER,
                $COL_TOP_PRODUCT_NAME TEXT,
                $COL_TOP_PRODUCT_STATS TEXT,
                $COL_TOP_PRODUCT_REVENUE REAL
            )
        """.trimIndent()

        val createAdminTable = """
            CREATE TABLE $TABLE_ADMIN_RECORDS (
                $COL_RECORD_ID TEXT PRIMARY KEY,
                $COL_TYPE TEXT,
                $COL_TITLE TEXT,
                $COL_SUBTITLE TEXT,
                $COL_RECORD_STATUS TEXT,
                $COL_ACTION_ONE TEXT,
                $COL_ACTION_TWO TEXT,
                $COL_ACTION_THREE TEXT
            )
        """.trimIndent()

        db.execSQL(createOrdersTable)
        db.execSQL(createSupplierTable)
        db.execSQL(createAdminTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ORDERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SUPPLIER_METRICS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ADMIN_RECORDS")
        onCreate(db)
    }


    fun saveUserOrder(orderId: String, userId: String, amount: Double, category: String, status: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_ORDER_ID, orderId)
            put(COL_USER_ID, userId)
            put(COL_AMOUNT, amount)
            put(COL_CATEGORY, category)
            put(COL_STATUS, status)
        }
        db.insertWithOnConflict(TABLE_ORDERS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getUserOrders(userId: String): List<LocalOrder> {
        val list = mutableListOf<LocalOrder>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_ORDERS,
            null,
            "$COL_USER_ID = ?",
            arrayOf(userId),
            null, null, null
        )
        cursor.use { c ->
            while (c.moveToNext()) {
                val orderId = c.getString(c.getColumnIndexOrThrow(COL_ORDER_ID))
                val uId = c.getString(c.getColumnIndexOrThrow(COL_USER_ID))
                val amount = c.getDouble(c.getColumnIndexOrThrow(COL_AMOUNT))
                val category = c.getString(c.getColumnIndexOrThrow(COL_CATEGORY))
                val status = c.getString(c.getColumnIndexOrThrow(COL_STATUS))
                list.add(LocalOrder(orderId, uId, amount, category, status))
            }
        }
        return list
    }


    fun saveSupplierMetrics(
        supplierId: String,
        revenue: Double,
        unitsSold: Int,
        topProductName: String,
        topProductStats: String,
        topProductRevenue: Double
    ) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_SUPPLIER_ID, supplierId)
            put(COL_REVENUE, revenue)
            put(COL_UNITS_SOLD, unitsSold)
            put(COL_TOP_PRODUCT_NAME, topProductName)
            put(COL_TOP_PRODUCT_STATS, topProductStats)
            put(COL_TOP_PRODUCT_REVENUE, topProductRevenue)
        }
        db.insertWithOnConflict(TABLE_SUPPLIER_METRICS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getSupplierMetrics(supplierId: String): LocalSupplierMetrics? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_SUPPLIER_METRICS,
            null,
            "$COL_SUPPLIER_ID = ?",
            arrayOf(supplierId),
            null, null, null
        )
        cursor.use { c ->
            if (c.moveToFirst()) {
                val rev = c.getDouble(c.getColumnIndexOrThrow(COL_REVENUE))
                val units = c.getInt(c.getColumnIndexOrThrow(COL_UNITS_SOLD))
                val topName = c.getString(c.getColumnIndexOrThrow(COL_TOP_PRODUCT_NAME))
                val topStats = c.getString(c.getColumnIndexOrThrow(COL_TOP_PRODUCT_STATS))
                val topRev = c.getDouble(c.getColumnIndexOrThrow(COL_TOP_PRODUCT_REVENUE))
                return LocalSupplierMetrics(supplierId, rev, units, topName, topStats, topRev)
            }
        }
        return null
    }


    fun saveAdminRecords(type: String, records: List<AdminRecord>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete(TABLE_ADMIN_RECORDS, "$COL_TYPE = ?", arrayOf(type))
            for (r in records) {
                val values = ContentValues().apply {
                    put(COL_RECORD_ID, r.documentId)
                    put(COL_TYPE, type)
                    put(COL_TITLE, r.title)
                    put(COL_SUBTITLE, r.subtitle)
                    put(COL_RECORD_STATUS, r.status)
                    put(COL_ACTION_ONE, r.actionOne)
                    put(COL_ACTION_TWO, r.actionTwo)
                    put(COL_ACTION_THREE, r.actionThree)
                }
                db.insertWithOnConflict(TABLE_ADMIN_RECORDS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getAdminRecords(type: String): List<AdminRecord> {
        val list = mutableListOf<AdminRecord>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_ADMIN_RECORDS,
            null,
            "$COL_TYPE = ?",
            arrayOf(type),
            null, null, null
        )
        cursor.use { c ->
            while (c.moveToNext()) {
                val docId = c.getString(c.getColumnIndexOrThrow(COL_RECORD_ID))
                val title = c.getString(c.getColumnIndexOrThrow(COL_TITLE))
                val subtitle = c.getString(c.getColumnIndexOrThrow(COL_SUBTITLE))
                val status = c.getString(c.getColumnIndexOrThrow(COL_RECORD_STATUS))
                val actionOne = c.getString(c.getColumnIndexOrThrow(COL_ACTION_ONE))
                val actionTwo = c.getString(c.getColumnIndexOrThrow(COL_ACTION_TWO))
                val actionThree = c.getString(c.getColumnIndexOrThrow(COL_ACTION_THREE))
                list.add(AdminRecord(docId, title, subtitle, status, actionOne, actionTwo, actionThree))
            }
        }
        return list
    }
}

data class LocalOrder(
    val orderId: String,
    val userId: String,
    val amount: Double,
    val category: String,
    val status: String
)

data class LocalSupplierMetrics(
    val supplierId: String,
    val revenue: Double,
    val unitsSold: Int,
    val topProductName: String,
    val topProductStats: String,
    val topProductRevenue: Double
)
