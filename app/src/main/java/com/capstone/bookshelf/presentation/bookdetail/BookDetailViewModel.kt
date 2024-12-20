package com.capstone.bookshelf.presentation.bookdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.capstone.bookshelf.app.Route
import com.capstone.bookshelf.domain.book.BookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class BookDetailViewModel(
    private val bookRepository: BookRepository,
    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    private val bookId = savedStateHandle.toRoute<Route.BookDetail>().bookId

    private val _state = MutableStateFlow(BookDetailState())
    val state = _state
//        .onStart {
//            fetchBookDescription()
//            observeFavoriteStatus()
//        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            _state.value
        )

    fun onAction(action: BookDetailAction) {
        when(action) {
            is BookDetailAction.OnSelectedBookChange -> {
                _state.update { it.copy(
                    book = action.book
                ) }
            }
//            is BookDetailAction.OnFavoriteClick -> {
//                viewModelScope.launch {
//                    if(state.value.isFavorite) {
//                        bookRepository.deleteFromFavorites(bookId)
//                    } else {
//                        state.value.book?.let { book ->
//                            bookRepository.markAsFavorite(book)
//                        }
//                    }
//                }
//            }
//            else -> Unit
        }
    }

//    private fun observeFavoriteStatus() {
//        bookRepository
//            .isBookFavorite(bookId)
//            .onEach { isFavorite ->
//                _state.update { it.copy(
//                    isFavorite = isFavorite
//                ) }
//            }
//            .launchIn(viewModelScope)
//    }
//
//    private fun fetchBookDescription() {
//        viewModelScope.launch {
//            bookRepository
//                .getBookDescription(bookId)
//                .onSuccess { description ->
//                    _state.update { it.copy(
//                        book = it.book?.copy(
//                            description = description
//                        ),
//                        isLoading = false
//                    ) }
//                }
//        }
//    }
}