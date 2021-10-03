package ru.gaket.themoviedb.presentation.moviedetails.view

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.gaket.themoviedb.R
import ru.gaket.themoviedb.core.navigation.AuthScreen
import ru.gaket.themoviedb.core.navigation.MoviesScreen
import ru.gaket.themoviedb.core.navigation.Navigator
import ru.gaket.themoviedb.core.navigation.ReviewScreen
import ru.gaket.themoviedb.core.navigation.Screen
import ru.gaket.themoviedb.databinding.FragmentMovieDetailsBinding
import ru.gaket.themoviedb.presentation.moviedetails.model.MovieDetailsEvent
import ru.gaket.themoviedb.presentation.moviedetails.model.MovieDetailsEvent.OpenAddReviewScreenEvent
import ru.gaket.themoviedb.presentation.moviedetails.model.MovieDetailsEvent.OpenAuthScreenEvent
import ru.gaket.themoviedb.presentation.moviedetails.model.MovieDetailsEvent.ShowErrorEvent
import ru.gaket.themoviedb.presentation.moviedetails.viewmodel.MovieDetailsState
import ru.gaket.themoviedb.presentation.moviedetails.viewmodel.MovieDetailsViewModel
import ru.gaket.themoviedb.util.diffedText
import javax.inject.Inject

@AndroidEntryPoint
class MovieDetailsFragment : Fragment(R.layout.fragment_movie_details) {

    private val viewModel: MovieDetailsViewModel by viewModels()

    private val binding by viewBinding(FragmentMovieDetailsBinding::bind)

    @Inject
    lateinit var navigator: Navigator

    private val reviewsAdapter by lazy {
        ReviewsAdapter(viewModel::onReviewClick, viewModel::onAddReviewClick)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.state.observe(viewLifecycleOwner, ::render)
        listenEvents()

        setupListeners()
        setupReviewsList()
        setupPoster()
    }

    private fun listenEvents() = lifecycleScope.launch {
        viewModel.events.flowWithLifecycle(lifecycle).collect(::handleEvent)
    }

    private fun handleEvent(event: MovieDetailsEvent) = when (event) {
        is OpenAuthScreenEvent -> AuthScreen().navigate()
        is OpenAddReviewScreenEvent -> ReviewScreen(event.movieId).navigate()
        is ShowErrorEvent -> showSnackError(event.errorMessageResId)
    }

    private fun render(state: MovieDetailsState) = with(binding) {
        pbMovieDetailsInfo.isVisible = state.isLoadingInfo
        gMovieDetailsInfo.isVisible = state.isLoadingInfo.not()

        pbMovieDetailsReviews.isVisible = state.isLoadingReviews && pbMovieDetailsInfo.isVisible.not()
        rvMovieDetailsReviews.isVisible = state.isLoadingReviews.not() && state.isLoadingInfo.not()

        tvMovieDetailsTitle.diffedText = state.title
        tvMovieDetailsYear.diffedText = state.year
        tvMovieDetailsGenres.diffedText = state.genres
        tvMovieDetailsOverview.diffedText = state.overview
        tvMovieDetailsRating.diffedText = state.rating
        updatePoster(state.posterUrl)
        reviewsAdapter.submitList(state.allReviews)
    }

    private fun updatePoster(posterUrl: String?) = with(binding) {
        if (ivMoviePoster.tag != posterUrl) {
            if (posterUrl.isNullOrBlank().not()) {
                Picasso.get()
                    .load(posterUrl)
                    .placeholder(R.drawable.ph_movie_grey_200)
                    .error(R.drawable.ph_movie_grey_200)
                    .fit()
                    .centerCrop()
                    .into(ivMoviePoster)
            }

            ivMoviePoster.tag = posterUrl
        }
    }

    private fun setupListeners() = with(binding) {
        ivMovieDetailsBack.setOnClickListener { MoviesScreen().navigate() }
        ivMovieDetailsBrowse.setOnClickListener { viewModel.onBrowseMovieClick() }
    }

    private fun setupReviewsList() {
        binding.rvMovieDetailsReviews.adapter = reviewsAdapter
    }

    private fun setupPoster() {
        binding.ivMoviePoster.clipToOutline = true
    }

    private fun showSnackError(@StringRes errorMessageResId: Int) {
        view?.let {
            Snackbar.make(it, errorMessageResId, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun Screen.navigate() = navigator.navigateTo(this)

    companion object {

        const val ARG_MOVIE_ID = "ARG_MOVIE_ID"
        const val ARG_MOVIE_TITLE = "ARG_MOVIE_TITLE"

        fun newInstance(movieId: Long, title: String) = MovieDetailsFragment().apply {
            arguments = bundleOf(
                ARG_MOVIE_ID to movieId,
                ARG_MOVIE_TITLE to title,
            )
        }
    }
}
