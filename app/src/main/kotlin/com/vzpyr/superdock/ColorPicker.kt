package com.vzpyr.superdock

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorPicker(
    color: Int,
    alpha: Int,
    onColorChange: (Int) -> Unit,
    onAlphaChange: (Int) -> Unit,
    inline: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val opaque = color or 0xFF000000.toInt()
    val hsv = remember(opaque) {
        FloatArray(3).also { AndroidColor.colorToHSV(opaque, it) }
    }
    var hue by remember(opaque) { mutableFloatStateOf(hsv[0]) }
    var saturation by remember(opaque) { mutableFloatStateOf(hsv[1] * 100f) }
    var value by remember(opaque) { mutableFloatStateOf(hsv[2] * 100f) }
    var expanded by remember { mutableStateOf(false) }

    fun emit() {
        onColorChange(
            0xFF000000.toInt() or
                AndroidColor.HSVToColor(floatArrayOf(hue, saturation / 100f, value / 100f)),
        )
    }

    val preview = Color(color.withAlpha(alpha))
    var hex by remember { mutableStateOf(TextFieldValue(hexOf(color))) }

    LaunchedEffect(opaque) {
        if (parseHex(hex.text) != opaque) {
            val text = hexOf(opaque)
            hex = TextFieldValue(text, selection = TextRange(text.length))
        }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            IconTooltip(stringResource(R.string.action_edit_color)) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(preview)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                        .clickable(
                            onClickLabel = stringResource(R.string.action_edit_color),
                        ) { expanded = true },
                )
            }
            OutlinedTextField(
                value = hex,
                onValueChange = { next ->
                    hex = next
                    parseHex(next.text)?.let { parsed ->
                        val nextHsv = FloatArray(3)
                        AndroidColor.colorToHSV(parsed, nextHsv)
                        hue = nextHsv[0]
                        saturation = nextHsv[1] * 100f
                        value = nextHsv[2] * 100f
                        onColorChange(parsed)
                    }
                },
                singleLine = true,
                label = { Text(stringResource(R.string.label_hex)) },
                modifier = Modifier.weight(1f),
            )
        }
        if (inline) {
            PickerBody(
                hue = hue,
                saturation = saturation,
                value = value,
                alpha = alpha,
                onSaturationValueChange = { s, v ->
                    saturation = s
                    value = v
                    emit()
                },
                onHueChange = {
                    hue = it
                    emit()
                },
                onAlphaChange = onAlphaChange,
            )
        }
    }

    if (expanded) {
        ModalBottomSheet(
            onDismissRequest = { expanded = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 32.dp),
            ) {
                SheetTitle(stringResource(R.string.sheet_color))
                PickerBody(
                    hue = hue,
                    saturation = saturation,
                    value = value,
                    alpha = alpha,
                    onSaturationValueChange = { s, v ->
                        saturation = s
                        value = v
                        emit()
                    },
                    onHueChange = {
                        hue = it
                        emit()
                    },
                    onAlphaChange = onAlphaChange,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
    }
}

@Composable
private fun PickerBody(
    hue: Float,
    saturation: Float,
    value: Float,
    alpha: Int,
    onSaturationValueChange: (Float, Float) -> Unit,
    onHueChange: (Float) -> Unit,
    onAlphaChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SaturationValueField(
            hue = hue,
            saturation = saturation,
            value = value,
            onChange = onSaturationValueChange,
        )
        HueSlider(hue = hue, onChange = onHueChange)
        Text(
            text = stringResource(R.string.color_opacity, alpha * 100 / 255),
            style = MaterialTheme.typography.bodyMedium,
        )
        Slider(
            value = alpha.toFloat(),
            onValueChange = { onAlphaChange(it.toInt()) },
            valueRange = 0f..255f,
        )
    }
}

@Composable
private fun SaturationValueField(
    hue: Float,
    saturation: Float,
    value: Float,
    onChange: (Float, Float) -> Unit,
) {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(
                Brush.horizontalGradient(
                    listOf(Color.White, Color(AndroidColor.HSVToColor(floatArrayOf(hue, 1f, 1f)))),
                ),
            )
            .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
            .onSizeChanged { size = it }
            .pointerInput(hue, size) {
                if (size == IntSize.Zero) return@pointerInput
                detectTapGestures { offset ->
                    onChange(
                        (offset.x / size.width).coerceIn(0f, 1f) * 100f,
                        (1f - offset.y / size.height).coerceIn(0f, 1f) * 100f,
                    )
                }
            }
            .pointerInput(hue, size) {
                if (size == IntSize.Zero) return@pointerInput
                var point = Offset.Zero
                detectDragGestures(
                    onDragStart = {
                        point = it
                        onChange(
                            (it.x / size.width).coerceIn(0f, 1f) * 100f,
                            (1f - it.y / size.height).coerceIn(0f, 1f) * 100f,
                        )
                    },
                ) { change, _ ->
                    point += change.position - change.previousPosition
                    onChange(
                        (point.x / size.width).coerceIn(0f, 1f) * 100f,
                        (1f - point.y / size.height).coerceIn(0f, 1f) * 100f,
                    )
                }
            },
    ) {
        if (size != IntSize.Zero) {
            val w = with(density) { size.width.toDp() }
            val h = with(density) { size.height.toDp() }
            Box(
                modifier = Modifier
                    .offset(
                        x = w * (saturation / 100f) - 10.dp,
                        y = h * (1f - value / 100f) - 10.dp,
                    )
                    .size(20.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.White, CircleShape),
            )
        }
    }
}

@Composable
private fun HueSlider(hue: Float, onChange: (Float) -> Unit) {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val stops = listOf(0f, 60f, 120f, 180f, 240f, 300f, 360f)
        .map { Color(AndroidColor.HSVToColor(floatArrayOf(it, 1f, 1f))) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(Brush.horizontalGradient(stops))
            .onSizeChanged { size = it }
            .pointerInput(size) {
                if (size == IntSize.Zero) return@pointerInput
                detectTapGestures {
                    onChange((it.x / size.width).coerceIn(0f, 1f) * 360f)
                }
            }
            .pointerInput(size) {
                if (size == IntSize.Zero) return@pointerInput
                var point = Offset.Zero
                detectDragGestures(
                    onDragStart = {
                        point = it
                        onChange((it.x / size.width).coerceIn(0f, 1f) * 360f)
                    },
                ) { change, _ ->
                    point += change.position - change.previousPosition
                    onChange((point.x / size.width).coerceIn(0f, 1f) * 360f)
                }
            },
    )
}

private fun hexOf(color: Int): String =
    String.format(Locale.ROOT, "#%06X", 0xFFFFFF and (color or 0xFF000000.toInt()))

private fun parseHex(text: String): Int? {
    val cleaned = text.removePrefix("#")
    if (cleaned.length != 6) return null
    return cleaned.toIntOrNull(16)?.let { 0xFF000000.toInt() or it }
}
