package com.capstone.bookshelf.data.book.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.capstone.bookshelf.data.book.database.entity.ChapterContentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChapterDao {
    @Transaction
    @Query("SELECT * FROM chapter_content WHERE bookId = :bookId AND tocId = :tocId")
    suspend fun getChapterContent(bookId: String,tocId: Int): ChapterContentEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertChapterContent(chapterContent: ChapterContentEntity)

    @Query("SELECT COUNT(*) FROM chapter_content WHERE bookId = :bookId")
    fun getPageSize(bookId: Int): Flow<Int>
}