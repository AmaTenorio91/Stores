package com.curso.stores

import android.app.Application
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Se aplica el patron Singleton con el objetivo de poder acceder desde cualquier parte de la aplicación
 * a la base de datos
 * */

class StoreApplication : Application() {

    companion object {
        private var storeDatabase: StoreDatabase? = null
        public fun getStoreDb (): StoreDatabase {
            return storeDatabase!!
        }
    }
    override fun onCreate() {
        super.onCreate()

        /*val MIGRATION_1_2 = object : Migration(1,2){
            override fun migrate(database: SupportSQLiteDatabase) {
                //En este apartado de la funcion se pone lo que exactamente va hacer la base de datos
                database.execSQL("ALTER TABLE StoreEntity ADD COLUMN  photoUrl TEXT NOT NULL DEFAULT ''")
            }
        }*/

        storeDatabase = StoreDatabase.getDb(applicationContext)
    }

}