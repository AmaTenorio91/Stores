package com.curso.stores

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = arrayOf(StoreEntity::class), version = 2, exportSchema = false)
abstract class StoreDatabase : RoomDatabase(){

    abstract fun storeDao(): StoreDao

    companion object {

        val MIGRATION_1_2 = object : Migration(1,2){
            override fun migrate(database: SupportSQLiteDatabase) {
                //En este apartado de la funcion se pone lo que exactamente va hacer la base de datos
                database.execSQL("ALTER TABLE StoreEntity ADD COLUMN  photoUrl TEXT NOT NULL DEFAULT ''")
            }
        }

        private var db :StoreDatabase? = null

        fun getDb (context: Context): StoreDatabase {
            if(db == null){
               db = Room.databaseBuilder(context.applicationContext, StoreDatabase::class.java, "StoreDatabase")
                   .addMigrations(MIGRATION_1_2).build()
            }
            return db!!
        }
    }

}