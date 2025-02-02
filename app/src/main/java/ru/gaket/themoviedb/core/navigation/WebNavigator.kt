package ru.gaket.themoviedb.core.navigation

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import ru.gaket.themoviedb.core.MovieUrlProvider
import javax.inject.Inject

/**
 * Class responsible for the browser navigation
 */
interface WebNavigator {

    fun navigateTo(movieId: Long)
}

class WebNavigatorImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val movieUrlProvider: MovieUrlProvider
) : WebNavigator {

    /**
     * Open a browser for a given url
     */
    override fun navigateTo(movieId: Long) {
        val browserIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("${movieUrlProvider.browseMovieBaseUrl}${movieId}")
        )
        browserIntent.flags = browserIntent.flags or Intent.FLAG_ACTIVITY_NEW_TASK
        return try {
            context.startActivity(browserIntent)
        } catch (e: ActivityNotFoundException) {
        }
    }
}
