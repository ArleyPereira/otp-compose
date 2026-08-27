package br.com.arleypereira.ohteepee

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import br.com.arleypereira.ohteepee.configuration.OhTeePeeConfigurations
import br.com.arleypereira.ohteepee.configuration.OhTeePeeErrorAnimationConfig
import br.com.arleypereira.ohteepee.example.BasicOhTeePeeExample
import br.com.arleypereira.ohteepee.utils.EMPTY
import br.com.arleypereira.ohteepee.utils.requestFocusSafely

private const val NOT_ENTERED_VALUE = '₺'

/**
 * A request to focus [cellIndex]. Every request is a distinct instance so that
 * asking for the same cell twice in a row still restarts the effect that
 * performs it.
 */
private class PendingFocusRequest(val cellIndex: Int)

/**
 * OhTeePeeInput is a composable that can be used to get OTP/Pin from user.
 *
 * Whenever the user edits the text, [onValueChange] is called with the most up to date state
 * including the empty values that represented by [OhTeePeeDefaults.PLACE_HOLDER].
 *
 * When the user fills all the cells, [onValueChange]'s isValid parameter will be `true`,
 * otherwise it will be `false`.
 *
 * To customize the appearance of cells you can pass [configurations] parameter with
 * a lot of options like , [OhTeePeeConfigurations.cellModifier], [OhTeePeeConfigurations.errorCellConfig] and more.
 *
 * If you don't want to pass all the configurations, you can use [OhTeePeeDefaults.inputConfiguration] to customize
 * only the configurations you want.
 *
 * @param value will be split to chars and shown in the [OhTeePeeInput].
 * @param onValueChange Called when user enters or deletes any cell.
 * @param configurations [OhTeePeeConfigurations] to customize the appearance of cells.
 *
 * @param modifier optional [Modifier] for the whole [OhTeePeeInput].
 * You can use [OhTeePeeConfigurations.cellModifier] to customize each single cell.
 *
 * @param isValueInvalid when set to true, all cells will use [OhTeePeeConfigurations.errorCellConfig] and
 * focus will be requested on first cell. you should set this to false when user starts editing the text.
 *
 * @param keyboardType The keyboard type to be used for this text field.
 *
 * @param enabled when enabled is false, user will not be able to interact with the text fields.
 * you can set it to false when you are waiting for a response from server.
 *
 * @param autoFocusByDefault when set to true, first cell will be focused by default.
 *
 * @param layoutDirection it can be used to specify/reverse the layout direction.
 *
 * @param horizontalArrangement it can be used to specify the horizontal arrangement of cells when
 * the size of the OhTeePeeInput is larger than the sum of its children sizes.
 *
 * @param divider an optional slot that will be used to draw a divider between cells.
 * It's placed at the end of the cell.
 *
 * @sample BasicOhTeePeeExample
 */
@Composable
fun OhTeePeeInput(
    value: String,
    onValueChange: (newValue: String, isValid: Boolean) -> Unit,
    configurations: OhTeePeeConfigurations,
    modifier: Modifier = Modifier,
    isValueInvalid: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.NumberPassword,
    imeAction: ImeAction = ImeAction.Next,
    callbackOnNext: () -> Unit = {},
    callbackOnDone: () -> Unit = {},
    callbackOnPrevious: () -> Unit = {},
    callbackOnSearch: () -> Unit = {},
    callbackOnSend: () -> Unit = {},
    callbackOnGo: () -> Unit = {},
    enabled: Boolean = true,
    keyboardController: SoftwareKeyboardController? = null,
    autoFocusByDefault: Boolean = true,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    layoutDirection: LayoutDirection = LocalLayoutDirection.current,
    divider: @Composable (RowScope.(cellIndex: Int) -> Unit)? = { Spacer(modifier = Modifier.width(8.dp)) },
) {
    require(configurations.placeHolder.length <= 1) {
        "placeHolder can't be more then 1 characters"
    }
    require(configurations.obscureText.length <= 1) {
        "obscureText can't be more then 1 characters"
    }

    val shakeErrorAnimatable = configurations.errorAnimationConfig
        ?.takeIf { it is OhTeePeeErrorAnimationConfig.Shake }
        ?.let { remember { Animatable(0f) } }

    val obscureText = configurations.obscureText
    val placeHolder = configurations.placeHolder
    val cellsCount = configurations.cellsCount
    val placeHolderAsChar = placeHolder.first()
    val otpValueCharArray: CharArray by remember(value, isValueInvalid) {
        val charArray = getOtpValueCharArray(
            cellsCount = cellsCount,
            shouldClearInput = isValueInvalid && configurations.clearInputOnError,
            value = value,
        )
        mutableStateOf(charArray)
    }
    val focusRequester = remember(cellsCount) { List(cellsCount) { FocusRequester() } }
    val transparentTextSelectionColors: TextSelectionColors = remember {
        TextSelectionColors(
            handleColor = Transparent,
            backgroundColor = Transparent,
        )
    }
    val visualTransformation = remember(obscureText) {
        if (obscureText.isEmpty()) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation(obscureText.first())
        }
    }

    // Focus is never moved straight from the value change callback: while the
    // IME is still delivering the current input, Compose tears the input
    // session of the cell that loses focus down and starts the next one in a
    // separate pass, which hides and re-opens the soft keyboard on every
    // keystroke. Recording the request as state and honouring it from an
    // effect moves the focus after that input has been processed, so the
    // keyboard stays up.
    var pendingFocusRequest: PendingFocusRequest? by remember { mutableStateOf(null) }

    LaunchedEffect(pendingFocusRequest) {
        val targetIndex = pendingFocusRequest?.cellIndex ?: return@LaunchedEffect
        focusRequester.getOrNull(targetIndex)?.requestFocusSafely()
    }

    fun moveFocus(currentIndex: Int, targetIndex: Int) {
        if (currentIndex == targetIndex || targetIndex !in (0 until cellsCount)) return

        pendingFocusRequest = PendingFocusRequest(targetIndex)
    }

    if (autoFocusByDefault) {
        LaunchedEffect(Unit) {
            val length = value.trim().length
            val focusIndex = length.coerceAtLeast(1) - 1
            focusRequester[focusIndex].requestFocusSafely()
        }
    }

    if (isValueInvalid) {
        LaunchedEffect(Unit) {
            if (configurations.clearInputOnError) {
                focusRequester.first().requestFocus()
                onValueChange("", false)
            }

            if (configurations.errorAnimationConfig != null) {
                triggerErrorAnimation(configurations.errorAnimationConfig, shakeErrorAnimatable)
            }
        }
    }

    OhTeePeeInput(
        modifier = modifier
            .then(
                if (shakeErrorAnimatable != null) {
                    Modifier.graphicsLayer {
                        translationX = shakeErrorAnimatable.value
                    }
                } else {
                    Modifier
                },
            ),
        textSelectionColors = transparentTextSelectionColors,
        cellsCount = cellsCount,
        otpValue = otpValueCharArray,
        placeHolder = placeHolder,
        isErrorOccurred = isValueInvalid,
        keyboardType = keyboardType,
        divider = divider,
        ohTeePeeConfigurations = configurations,
        focusRequesters = focusRequester,
        enabled = enabled,
        visualTransformation = visualTransformation,
        layoutDirection = layoutDirection,
        horizontalArrangement = horizontalArrangement,
        onCellInputChange = { currentCellIndex, newValue ->
            handleCellInputChange(
                otpValueCharArray = otpValueCharArray,
                currentCellIndex = currentCellIndex,
                newValue = newValue,
                obscureText = obscureText,
                cellsCount = cellsCount,
                onValueChange = onValueChange,
                placeHolderAsChar = placeHolderAsChar,
                moveFocus = ::moveFocus,
            )
        },
        imeAction = imeAction,
        callbackOnNext = callbackOnNext,
        callbackOnDone = callbackOnDone,
        callbackOnPrevious = callbackOnPrevious,
        callbackOnSearch = callbackOnSearch,
        callbackOnSend = callbackOnSend,
        callbackOnGo = callbackOnGo,
        keyboardController = keyboardController,
    )
}

private suspend fun triggerErrorAnimation(
    animationConfig: OhTeePeeErrorAnimationConfig,
    shakeErrorAnimatable: Animatable<Float, AnimationVector1D>?,
) {
    when (animationConfig) {
        is OhTeePeeErrorAnimationConfig.Shake -> {
            shakeErrorAnimatable ?: return

            repeat(animationConfig.repeat) { index ->
                val targetValue =
                    (if (index % 2 == 0) -1 else 1) * animationConfig.translationXRange
                shakeErrorAnimatable.animateTo(
                    targetValue = targetValue,
                    animationSpec = animationConfig.animationSpec,
                )
            }
        }
    }
}

@Composable
private fun OhTeePeeInput(
    modifier: Modifier = Modifier,
    textSelectionColors: TextSelectionColors,
    cellsCount: Int,
    otpValue: CharArray,
    placeHolder: String,
    isErrorOccurred: Boolean,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    ohTeePeeConfigurations: OhTeePeeConfigurations,
    focusRequesters: List<FocusRequester>,
    enabled: Boolean,
    visualTransformation: VisualTransformation,
    onCellInputChange: (index: Int, value: String) -> Unit,
    horizontalArrangement: Arrangement.Horizontal,
    layoutDirection: LayoutDirection,
    callbackOnNext: () -> Unit,
    callbackOnDone: () -> Unit,
    callbackOnPrevious: () -> Unit,
    callbackOnSearch: () -> Unit,
    callbackOnSend: () -> Unit,
    callbackOnGo: () -> Unit,
    keyboardController: SoftwareKeyboardController? = null,
    divider: @Composable (RowScope.(cellIndex: Int) -> Unit)?,
) {
    CompositionLocalProvider(
        LocalTextSelectionColors provides textSelectionColors,
        LocalLayoutDirection provides layoutDirection,
    ) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = horizontalArrangement,
        ) {
            repeat(cellsCount) { index ->
                val displayValue = remember(otpValue[index]) {
                    getCellDisplayCharacter(currentChar = otpValue[index])
                }
                OhTeePeeCell(
                    value = displayValue,
                    isErrorOccurred = isErrorOccurred,
                    keyboardType = keyboardType,
                    modifier = Modifier
                        .then(ohTeePeeConfigurations.cellModifier)
                        .focusRequester(focusRequester = focusRequesters[index]),
                    enabled = enabled,
                    configurations = ohTeePeeConfigurations,
                    onValueChange = { onCellInputChange(index, it) },
                    placeHolder = placeHolder,
                    visualTransformation = visualTransformation,
                    imeAction = imeAction,
                    callbackOnNext = callbackOnNext,
                    callbackOnDone = callbackOnDone,
                    callbackOnPrevious = callbackOnPrevious,
                    callbackOnSearch = callbackOnSearch,
                    callbackOnSend = callbackOnSend,
                    callbackOnGo = callbackOnGo,
                    keyboardController = keyboardController,
                )
                if (divider != null && index != cellsCount - 1) {
                    divider(index)
                }
            }
        }
    }
}

private fun getOtpValueCharArray(cellsCount: Int, shouldClearInput: Boolean, value: String): CharArray {
    val charList = CharArray(cellsCount) { index ->
        if (shouldClearInput) {
            NOT_ENTERED_VALUE
        } else {
            value.getOrNull(index)
                ?.takeIf { it.isWhitespace().not() }
                ?: NOT_ENTERED_VALUE
        }
    }
    return charList
}

private fun handleCellInputChange(
    otpValueCharArray: CharArray,
    currentCellIndex: Int,
    newValue: String,
    obscureText: String,
    cellsCount: Int,
    onValueChange: (newValue: String, isValid: Boolean) -> Unit,
    placeHolderAsChar: Char,
    moveFocus: (currentIndex: Int, targetIndex: Int) -> Unit,
) {
    val currentCellText = otpValueCharArray[currentCellIndex].toString()
    val formattedNewValue = newValue.replace(obscureText, String.EMPTY)

    if (formattedNewValue == currentCellText) {
        moveFocus(currentCellIndex, currentCellIndex + 1)
        return
    }

    if (formattedNewValue.length == cellsCount) {
        onValueChange(formattedNewValue, true)
        moveFocus(currentCellIndex, cellsCount - 1)
        return
    }

    if (formattedNewValue.isNotEmpty()) {
        otpValueCharArray[currentCellIndex] = formattedNewValue.last()
        moveFocus(currentCellIndex, currentCellIndex + 1)
    } else if (currentCellText != NOT_ENTERED_VALUE.toString()) {
        otpValueCharArray[currentCellIndex] = NOT_ENTERED_VALUE
    } else {
        val previousIndex = (currentCellIndex - 1).coerceIn(0, cellsCount)
        otpValueCharArray[previousIndex] = NOT_ENTERED_VALUE
        moveFocus(currentCellIndex, previousIndex)
    }
    val otpValueAsString = otpValueCharArray.joinToString(String.EMPTY) {
        if (it == NOT_ENTERED_VALUE) " " else it.toString()
    }
    onValueChange(otpValueAsString, otpValueAsString.none { it == placeHolderAsChar })
}

private fun getCellDisplayCharacter(currentChar: Char): String =
    if (currentChar == NOT_ENTERED_VALUE) String.EMPTY else currentChar.toString()
