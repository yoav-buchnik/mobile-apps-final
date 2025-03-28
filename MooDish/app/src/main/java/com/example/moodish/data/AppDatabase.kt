package com.example.moodish.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.moodish.data.dao.PostDao
import com.example.moodish.data.dao.UserDao
import com.example.moodish.data.model.Post
import com.example.moodish.data.model.User

@Database(entities = [User::class, Post::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "moodish_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE posts_new (
                        id TEXT NOT NULL PRIMARY KEY,
                        email TEXT NOT NULL,
                        text TEXT NOT NULL,
                        imageUrl TEXT,
                        label TEXT,
                        lastUpdated INTEGER
                    )
                """)
                
                database.execSQL("""
                    INSERT INTO posts_new (id, email, text, imageUrl, label, lastUpdated)
                    SELECT hex(randomblob(16)), email, text, imageUrl, label, lastUpdated
                    FROM posts
                """)
                
                database.execSQL("DROP TABLE posts")
                database.execSQL("ALTER TABLE posts_new RENAME TO posts")
            }
        }
    }
}
