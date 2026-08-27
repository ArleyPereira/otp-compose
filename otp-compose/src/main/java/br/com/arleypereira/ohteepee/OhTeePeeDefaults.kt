package br.com.arleypereira.ohteepee

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.com.arleypereira.ohteepee.configuration.OhTeePeeCellBackground
import br.com.arleypereira.ohteepee.configuration.OhTeePeeCellConfiguration
import br.com.arleypereira.ohteepee.configuration.OhTeePeeConfigurations
import br.com.arleypereira.ohteepee.configuration.OhTeePeeErrorAnimationConfig
import br.com.arleypereira.ohteepee.utils.EMPTY

object OhTeePeeDefaults {
    val BORDER_WIDTH = 1.dp
    const val PLACE_HOLDER = ' '
    val defaultShakeAnimationSpec: AnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioHighBouncy,
        stiffness = 10_000_000f,
    )

    @Composable
    fun inputConfiguration(
        cellsCount: Int,
        emptyCellConfig: OhTeePeeCellConfiguration,
        filledCellConfig: OhTeePeeCellConfiguration = emptyCellConfig,
        activeCellConfig: OhTeePeeCellConfiguration = emptyCellConfig.copy(
            borderColor = MaterialTheme.colors.primary,
        ),
        errorCellConfig: OhTeePeeCellConfiguration = emptyCellConfig.copy(
            borderColor = MaterialTheme.colors.error,
        ),
        cellModifier: Modifier = Modifier.size(48.dp),
        elevation: Dp = 0.dp,
        cursorColor: Color = Color.Transparent,
        clearInputOnError: Boolean = true,
        enableBottomLine: Boolean = false,
        obscureText: String = String.EMPTY,
        placeHolder: String = PLACE_HOLDER.toString(),
        errorAnimationConfig: OhTeePeeErrorAnimationConfig? = OhTeePeeErrorAnimationConfig.Shake(),
    ) = OhTeePeeConfigurations(
        cellModifier = cellModifier,
        elevation = elevation,
        activeCellConfig = activeCellConfig,
        errorCellConfig = errorCellConfig,
        emptyCellConfig = emptyCellConfig,
        filledCellConfig = filledCellConfig,
        cursorColor = cursorColor,
        clearInputOnError = clearInputOnError,
        enableBottomLine = enableBottomLine,
        placeHolder = placeHolder,
        obscureText = obscureText,
        cellsCount = cellsCount,
        errorAnimationConfig = errorAnimationConfig,
    )

    /**
     * Kept for callers that configure a plain background colour. [backgroundColor] deliberately has
     * no default value: with one, every call that names neither [backgroundColor] nor
     * [OhTeePeeCellBackground] — including `cellConfiguration()` itself — matches this overload and
     * the [OhTeePeeCellBackground] one equally well, which Kotlin rejects as an overload resolution
     * ambiguity.
     */
    @Composable
    fun cellConfiguration(
        backgroundColor: Color,
        shape: Shape = MaterialTheme.shapes.medium,
        borderColor: Color = MaterialTheme.colors.primary,
        borderWidth: Dp = BORDER_WIDTH,
        textStyle: TextStyle = TextStyle(),
        placeHolderTextStyle: TextStyle = textStyle,
    ) = OhTeePeeCellConfiguration(
        shape = shape,
        cellBackground = OhTeePeeCellBackground.Solid(backgroundColor),
        borderColor = borderColor,
        borderWidth = borderWidth,
        textStyle = textStyle,
        placeHolderTextStyle = placeHolderTextStyle,
    )

    @Composable
    fun cellConfiguration(
        shape: Shape = MaterialTheme.shapes.medium,
        cellBackground: OhTeePeeCellBackground = OhTeePeeCellBackground.Solid(MaterialTheme.colors.surface),
        borderColor: Color = MaterialTheme.colors.primary,
        borderWidth: Dp = BORDER_WIDTH,
        textStyle: TextStyle = TextStyle(),
        placeHolderTextStyle: TextStyle = textStyle,
    ) = OhTeePeeCellConfiguration(
        shape = shape,
        cellBackground = cellBackground,
        borderColor = borderColor,
        borderWidth = borderWidth,
        textStyle = textStyle,
        placeHolderTextStyle = placeHolderTextStyle,
    )
}
