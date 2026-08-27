![](ohteepee_cover.png)

# OhTeePee ![latestVersion](https://img.shields.io/github/v/tag/ArleyPereira/ohteepee?display_name=tag)

> ## About this fork
>
> A fork of [composeuisuite/ohteepee](https://github.com/composeuisuite/ohteepee), maintained here
> because the original project is no longer active. It has been modified: the package moved to
> `br.com.arleypereira.ohteepee`, the soft keyboard no longer collapses on every keystroke, and the
> build targets current Compose.
>
> **Requires `minSdk` 23 and `compileSdk` 37.**

## Demo

![ohteepee_demo_1](ohteepee_demo_1.gif)

![ohteepee_demo_2](ohteepee_demo_2.gif)

![ohteepee_demo_4](ohteepee_demo_4.gif)

![ohteepee_demo_5](ohteepee_demo_5.gif)

![ohteepee_demo_3](ohteepee_demo_3.gif)

## Implementation

Add the Jitpack repository to your root build.gradle file. If you’re using the settings.gradle file, include it there.

```
repositories {
    ...
    maven { url 'https://jitpack.io' }
}
```

Then add OhTeePee dependency to your module build.gradle file.

```groovy
implementation "com.github.ArleyPereira:ohteepee:$versionName"
```

### Usage

First of all, create a basic composable then start to build on it.

```kotlin
@Composable
fun OtpInput() {
    // a mutable state to handle OTP value changes…
    var otpValue: String by remember { mutableStateOf("") }

    // this config will be used for each cell
    val defaultCellConfig = OhTeePeeDefaults.cellConfiguration(
        borderColor = Color.LightGray,
        borderWidth = 1.dp,
        shape = RoundedCornerShape(16.dp),
        textStyle = TextStyle(
            color = Color.Black
        )
    )

    OhTeePeeInput(
        value = otpValue,
        onValueChange = { newValue, isValid ->
            otpValue = newValue
        },
        configurations = OhTeePeeDefaults.inputConfiguration(
            cellsCount = 6,
            emptyCellConfig = defaultCellConfig,
            cellModifier = Modifier.size(48.dp),
        ),
    )
}
```

To customize it further, you can use config parameters such as **filledCellConfig**, **activeCellConfig**, **errorCellConfig** and **errorAnimationConfig** to create different UI behaviour for different situations.

```kotlin
@Composable
fun OtpInput() {
    ...

    OhTeePeeInput(
        ...
        configurations = OhTeePeeDefaults.inputConfiguration(
            ...,
            emptyCellConfig = defaultCellConfig,
            filledCellConfig = defaultCellConfig,
            activeCellConfig = defaultCellConfig.copy(
                borderColor = Color.Blue,
                borderWidth = 2.dp
            ),
            errorCellConfig = defaultCellConfig.copy(
                borderColor = Color.Red,
                borderWidth = 2.dp
            ),
            errorAnimationConfig = null, // default is OhTeePeeErrorAnimationConfig.Shake(),
            placeHolder = "-",
        ),
    )
}
```

Optionally, you can insert a **divider** between cells like a padding or a dash.

```kotlin
@Composable
fun OtpInput() {
    OhTeePeeInput(
        ...
        divider = { index -> 
            Row {
                Spacer(modifier = Modifier.width(4.dp))
                if (index == 1) {
                    Text(" - ", color = Color.White)
                }
                Spacer(modifier = Modifier.width(4.dp))
            } 
        }
    )
}
```

This is just the tip of the iceberg when it comes to customization of OhTeePee. The
[sample app](sample) shows every option in context.

## Todo List

- [x] Read OTP Code directly from SMS
- [x] Add animations

## Contributing

We are always open to new ideas! To contribute, please check following steps:

1. Open an issue first to discuss what you would like to change.
1. Fork the Project
1. Create your feature branch (`git checkout -b feature/new_feature`)
1. Format code using Ktlint (`./gradlew ktlintFormat`)
1. Commit your changes (`git commit -m 'Add some new feature'`)
1. Push to the branch (`git push origin feature/new_feature`)
1. Open a pull request

## Show your support

⭐️ Give us a star if this project helped you! ⭐️

### [LICENSE](LICENSE.md)

OhTeePee is licensed under the Apache License 2.0. This is a modified version of the original work by
the OhTeePee authors; their copyright and that license are retained, and the modifications are
released under the same terms.
