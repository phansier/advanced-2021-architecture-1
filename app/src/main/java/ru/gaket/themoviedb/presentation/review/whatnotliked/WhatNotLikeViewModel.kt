package ru.gaket.themoviedb.presentation.review.whatnotliked

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.gaket.themoviedb.domain.review.repository.ReviewRepository
import ru.gaket.themoviedb.presentation.review.ReviewFieldEvent
import javax.inject.Inject

@HiltViewModel
class WhatNotLikeViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository,
) : ViewModel() {

    private val _events = MutableSharedFlow<ReviewFieldEvent>()
    val events: LiveData<ReviewFieldEvent>
        get() = _events
            .asLiveData(viewModelScope.coroutineContext)

    val initialValue: LiveData<String> = reviewRepository.reviewState
        .map { it.whatDidNotLike }
        .filterNotNull()
        .asLiveData(viewModelScope.coroutineContext)

    fun submitInfo(whatDidNotLike: String) {
        viewModelScope.launch {
            val fieldEvent = if (whatDidNotLike.isBlank()) {
                ReviewFieldEvent.EMPTY_FIELD
            } else {
                reviewRepository.setWhatDidNotLike(whatDidNotLike)
                ReviewFieldEvent.SUCCESS
            }

            _events.emit(fieldEvent)
        }
    }
}
