package br.com.arleypereira.ohteepee.utils

import android.util.Log
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester

private const val FOCUS_REQUEST_LOG_TAG = "OhTeePee"

internal val String.Companion.EMPTY: String
    get() = ""

internal fun FocusRequester.requestFocusSafely() {
    try {
        this.requestFocus()
    } catch (e: Exception) {
        // Requesting focus on a cell that is not attached to the layout throws. Report it instead
        // of printing a bare stack trace, so it is greppable when it does happen.
        Log.e(FOCUS_REQUEST_LOG_TAG, "Could not move focus to an OhTeePee cell", e)
    }
}

internal fun Modifier.conditional(condition: Boolean, modifier: Modifier.() -> Modifier): Modifier = if (condition) {
    then(modifier(Modifier))
} else {
    this
}
