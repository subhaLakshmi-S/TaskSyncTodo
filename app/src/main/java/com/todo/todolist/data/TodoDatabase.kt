package com.todo.todolist.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Todo::class], version = 2, exportSchema = true)
abstract class TodoDatabase : RoomDatabase() {
    abstract fun todoDao() : TodoDao

    companion object{
        @Volatile
        private var INSTANCE: TodoDatabase? = null
        fun getDatabase(context: Context): TodoDatabase {
            return INSTANCE ?: synchronized(this){
                Room.databaseBuilder(context.applicationContext, TodoDatabase::class.java,"TODO database")
                    .fallbackToDestructiveMigration(false)
                    .build().also{ INSTANCE =it }
            }
        }

    }


}