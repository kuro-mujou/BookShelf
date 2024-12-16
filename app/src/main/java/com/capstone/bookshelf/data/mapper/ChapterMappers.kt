package com.capstone.bookshelf.data.mapper

import com.capstone.bookshelf.data.book.database.entity.ChapterContentEntity
import com.capstone.bookshelf.domain.wrapper.Chapter

fun Chapter.toEntity(): ChapterContentEntity {
    return ChapterContentEntity(
        chapterContentId = chapterContentId,
        tocId = tocId,
        bookId = bookId,
        chapterTitle = chapterTitle,
        content = content,
    )
}

fun ChapterContentEntity.toDataClass(): Chapter {
    return Chapter(
        chapterContentId = chapterContentId,
        tocId = tocId,
        bookId = bookId,
        chapterTitle = chapterTitle,
        content = content,
    )
}