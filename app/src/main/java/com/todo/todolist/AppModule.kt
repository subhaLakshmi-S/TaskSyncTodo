package com.todo.todolist

import android.content.Context
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import com.todo.todolist.data.TodoDao
import com.todo.todolist.data.TodoDatabase
import com.todo.todolist.firebaseAuth.AuthUtil
import com.todo.todolist.repository.TodoRepository
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext app: Context): TodoDatabase{
        return Room.databaseBuilder(app, TodoDatabase::class.java, "TODO database").build()
    }

    @Provides
    fun provideTodoDao(db: TodoDatabase): TodoDao{
        return db.todoDao()
    }

    @Provides
    @Singleton
    fun provideRepository(todoDao: TodoDao,authUtil: AuthUtil): TodoRepository{
        return TodoRepository(todoDao, authUtil)
    }
    @Provides
    @Singleton
    fun provideAuthUtil(): AuthUtil{
        return AuthUtil()
    }
}