package com.leaf.qrcodegenerator.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.leaf.qrcodegenerator.R
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController
import top.yukonga.miuix.kmp.theme.defaultTextStyles

val PageHorizontalPadding = 16.dp

@OptIn(ExperimentalTextApi::class)
val MiSansFontFamily = FontFamily(
    Font(R.font.misans_vf, FontWeight.Light, variationSettings = FontVariation.Settings(FontVariation.weight(300))),
    Font(R.font.misans_vf, FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.misans_vf, FontWeight.Medium, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(R.font.misans_vf, FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
    Font(R.font.misans_vf, FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(700))),
    Font(R.font.misans_vf, FontWeight.ExtraBold, variationSettings = FontVariation.Settings(FontVariation.weight(800))),
    Font(R.font.misans_vf, FontWeight.Black, variationSettings = FontVariation.Settings(FontVariation.weight(900))),
)

private fun miSansStyle(
    size: Int,
    weight: FontWeight = FontWeight.Normal,
    lineHeight: TextStyle.() -> TextStyle = { this },
): TextStyle = TextStyle(
    fontFamily = MiSansFontFamily,
    fontSize = size.sp,
    fontWeight = weight,
).lineHeight()

private val AppTextStyles = defaultTextStyles(
    main = miSansStyle(14),
    paragraph = miSansStyle(17) { copy(lineHeight = 1.2.em) },
    body1 = miSansStyle(16),
    body2 = miSansStyle(14),
    button = miSansStyle(17, FontWeight.SemiBold),
    footnote1 = miSansStyle(13),
    footnote2 = miSansStyle(11),
    headline1 = miSansStyle(17, FontWeight.Medium),
    headline2 = miSansStyle(16, FontWeight.Medium),
    subtitle = miSansStyle(14, FontWeight.SemiBold),
    title1 = miSansStyle(32, FontWeight.Light),
    title2 = miSansStyle(24, FontWeight.Medium),
    title3 = miSansStyle(20, FontWeight.SemiBold),
    title4 = miSansStyle(18, FontWeight.Medium),
)

@Composable
fun QrCodeGeneratorTheme(content: @Composable () -> Unit) {
    val controller = remember { ThemeController(ColorSchemeMode.System) }
    MiuixTheme(
        controller = controller,
        textStyles = AppTextStyles,
        content = content,
    )
}
