package com.iteritory.searchlocationapp

import android.content.Context
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper


class DatabaseOpenHelper(context: Context) : SQLiteAssetHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private val DATABASE_NAME = "DataSQLite.db"
        private val DATABASE_VERSION = 1
    }
}