package ru.paramonov.cryptocurrencytracker.presentation.screens.deteilcoin.terminal.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.paramonov.cryptocurrencytracker.R
import ru.paramonov.cryptocurrencytracker.data.network.model.graphic.Bar
import ru.paramonov.cryptocurrencytracker.presentation.screens.deteilcoin.terminal.TerminalViewModel
import ru.paramonov.cryptocurrencytracker.presentation.screens.deteilcoin.terminal.TimeFrame
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

private const val MIN_VISIBLE_BARS_COUNT = 20

@Composable
fun Terminal(
    modifier: Modifier = Modifier
) {
    val viewModel: TerminalViewModel = viewModel()
    val viewState: State<TerminalViewState> = viewModel.viewState.collectAsState()

    TerminalContent(
        viewState = viewState,
        modifier = modifier,
        viewModel = viewModel
    )
}

@Composable
private fun TerminalContent(
    viewState: State<TerminalViewState>,
    viewModel: TerminalViewModel,
    modifier: Modifier
) {
    when (val currentState = viewState.value) {
        is TerminalViewState.ContentTerminal -> {
            val terminalState = rememberTerminalState(bars = currentState.bars)

            Chart(
                modifier = modifier,
                terminalState = terminalState,
                onChangeTerminalState = {
                    terminalState.value = it
                },
                timeFrame = currentState.timeFrame
            )

            currentState.bars.firstOrNull()?.let { bar ->
                Prices(
                    modifier = modifier,
                    terminalState = terminalState,
                    lastPrice = bar.closePrice
                )
            }

            TimeFrames(
                modifier = modifier,
                selectedFrame = currentState.timeFrame,
                onTimeFrameSelected = { timeFrame ->
                    viewModel.loadBars(timeFrame = timeFrame)
                }
            )
        }

        is TerminalViewState.Initial -> {}

        is TerminalViewState.Loading -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(color = Color.Black),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }

        is TerminalViewState.Error -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(color = Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = stringResource(id = currentState.messageResId), color = Color.White)
                    Spacer(modifier = modifier.height(height = 16.dp))
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun TimeFrames(
    modifier: Modifier = Modifier,
    selectedFrame: TimeFrame,
    onTimeFrameSelected: (TimeFrame) -> Unit
) {
    Row(
        modifier = modifier
            .padding(all = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TimeFrame.entries.forEach { timeFrames ->
            val stringResId = when (timeFrames) {
                TimeFrame.MIN_5 -> R.string.timeframe_5_minute
                TimeFrame.MIN_15 -> R.string.timeframe_15_minute
                TimeFrame.MIN_30 -> R.string.timeframe_30_minute
                TimeFrame.HOUR_1 -> R.string.timeframe_1_hour
                TimeFrame.DAY_1 -> R.string.timeframe_1_day
            }
            val isSelectedTimeFrames = selectedFrame == timeFrames
            AssistChip(
                onClick = { onTimeFrameSelected(timeFrames) },
                label = { Text(text = stringResource(id = stringResId)) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (isSelectedTimeFrames) Color.White else Color.Black,
                    labelColor = if (isSelectedTimeFrames) Color.Black else Color.White
                )
            )
        }
    }
}

@Composable
private fun Chart(
    modifier: Modifier = Modifier,
    terminalState: State<TerminalState>,
    onChangeTerminalState: (TerminalState) -> Unit,
    timeFrame: TimeFrame
) {
    val currentState = terminalState.value
    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        val visibleBarsCount = (currentState.visibleBarsCount / zoomChange).roundToInt()
            .coerceIn(minimumValue = MIN_VISIBLE_BARS_COUNT, maximumValue = currentState.bars.size)

        val scrolledBy = (currentState.scrolledBy + panChange.x)
            .coerceIn(
                minimumValue = 0f,
                maximumValue = currentState.bars.size * currentState.barWidth - currentState.terminalWidth
            )

        onChangeTerminalState(
            currentState.copy(
                visibleBarsCount = visibleBarsCount,
                scrolledBy = scrolledBy
            )
        )
    }

    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color.Black)
            .clipToBounds()
            .padding(top = 32.dp, bottom = 32.dp, end = 32.dp)
            .transformable(state = transformableState)
            .onSizeChanged { size ->
                onChangeTerminalState(
                    currentState.copy(
                        terminalWidth = size.width.toFloat(),
                        terminalHeight = size.height.toFloat()
                    )
                )
            }
    ) {
        val minPriceOnTime = currentState.minPriceOnTime
        val pxPerCount = currentState.pxPerCount

        translate(left = currentState.scrolledBy) {
            currentState.bars.forEachIndexed { index, bar ->
                val offsetX = size.width - index * currentState.barWidth

                drawTimeDelimiter(
                    bar = bar,
                    nextBar = if (index < currentState.bars.size - 1) {
                        currentState.bars[index + 1]
                    } else {
                        null
                    },
                    timeFrame = timeFrame,
                    textMeasurer = textMeasurer,
                    offsetX = offsetX
                )
                drawLine(
                    color = Color.White,
                    start = Offset(
                        x = offsetX,
                        y = size.height - ((bar.lowPrice - minPriceOnTime) * pxPerCount)
                    ),
                    end = Offset(
                        x = offsetX,
                        y = size.height - ((bar.highPrice - minPriceOnTime) * pxPerCount)
                    ),
                    strokeWidth = 1.dp.toPx()
                )
                val isIncreasePrice = bar.openPrice < bar.closePrice
                drawLine(
                    color = if (isIncreasePrice) Color.Green else Color.Red,
                    start = Offset(
                        x = offsetX,
                        y = size.height - ((bar.openPrice - minPriceOnTime) * pxPerCount)
                    ),
                    end = Offset(
                        x = offsetX,
                        y = size.height - ((bar.closePrice - minPriceOnTime) * pxPerCount)
                    ),
                    strokeWidth = currentState.barWidth / 2
                )
            }
        }
    }
}

@Composable
private fun Prices(
    modifier: Modifier = Modifier,
    terminalState: State<TerminalState>,
    lastPrice: Float
) {
    val textMeasurer = rememberTextMeasurer()
    val currentState = terminalState.value
    val maxPriceOnTime = currentState.maxPriceOnTime
    val minPriceOnTime = currentState.minPriceOnTime
    val pxPerCount = currentState.pxPerCount

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .padding(vertical = 32.dp)
    ) {
        drawPriceLine(
            maxPriceOnTime = maxPriceOnTime,
            minPriceOnTime = minPriceOnTime,
            lastPrice = lastPrice,
            pxPerCount = pxPerCount,
            textMeasurer = textMeasurer
        )
    }
}

private fun DrawScope.drawTimeDelimiter(
    bar: Bar,
    nextBar: Bar?,
    timeFrame: TimeFrame,
    textMeasurer: TextMeasurer,
    offsetX: Float
) {
    val calendar: Calendar = bar.calendar

    val minutes = calendar.get(Calendar.MINUTE)
    val hours = calendar.get(Calendar.HOUR_OF_DAY)
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

    val shouldDrawDelimiter = when (timeFrame) {
        TimeFrame.MIN_5 -> {
            minutes == 0
        }

        TimeFrame.MIN_15 -> {
            minutes == 0 && hours % 2 == 0
        }

        TimeFrame.MIN_30, TimeFrame.HOUR_1 -> {
            val nextBarDay = nextBar?.calendar?.get(Calendar.DAY_OF_MONTH)
            day != nextBarDay
        }

        TimeFrame.DAY_1 -> {
            val nextBarDayOfWeek = nextBar?.calendar?.get(Calendar.DAY_OF_WEEK)
            dayOfWeek != nextBarDayOfWeek && day % 2 == 0
        }
    }

    if (!shouldDrawDelimiter) return

    drawLine(
        color = Color.White.copy(alpha = 0.5f),
        start = Offset(x = offsetX, 0.dp.toPx()),
        end = Offset(x = offsetX, size.height),
        strokeWidth = 1.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(
            intervals = floatArrayOf(
                4.dp.toPx(), 4.dp.toPx()
            )
        )
    )

    val nameOfMouth = calendar.getDisplayName(
        Calendar.MONTH, Calendar.SHORT, Locale.getDefault()
    )
    val nameOfDay = calendar.getDisplayName(
        Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()
    )

    val text = when (timeFrame) {
        TimeFrame.MIN_5, TimeFrame.MIN_15 -> String.format(format = "%02d:00", hours)
        TimeFrame.MIN_30, TimeFrame.HOUR_1 -> String.format(format = "%s %s", day, nameOfDay)
        TimeFrame.DAY_1 -> String.format(format = "%s %s", day, nameOfMouth)
    }

    val textLayoutResult = textMeasurer.measure(
        text = text,
        style = TextStyle(
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    )
    drawText(
        textLayoutResult = textLayoutResult,
        topLeft = Offset(x = offsetX - textLayoutResult.size.width / 2, y = size.height)
    )
}

private fun DrawScope.drawPriceLine(
    maxPriceOnTime: Float,
    minPriceOnTime: Float,
    lastPrice: Float,
    pxPerCount: Float,
    textMeasurer: TextMeasurer
) {
    // max price
    val maxPriceOffsetY = 0.dp.toPx()
    drawDashedLine(
        start = Offset(x = 0.dp.toPx(), y = maxPriceOffsetY),
        end = Offset(x = size.width, maxPriceOffsetY)
    )
    drawTextPrice(
        price = maxPriceOnTime,
        textMeasurer = textMeasurer,
        offsetY = maxPriceOffsetY
    )
    // last price
    val lastPriceOffsetY = size.height - ((lastPrice - minPriceOnTime) * pxPerCount)
    drawDashedLine(
        start = Offset(x = 0.dp.toPx(), y = lastPriceOffsetY),
        end = Offset(x = size.width, y = lastPriceOffsetY)
    )
    drawTextPrice(
        price = lastPrice,
        textMeasurer = textMeasurer,
        offsetY = lastPriceOffsetY
    )
    // min price
    val minPriceOffsetY = size.height
    drawDashedLine(
        start = Offset(x = 0.dp.toPx(), y = minPriceOffsetY),
        end = Offset(x = size.width, y = minPriceOffsetY)
    )
    drawTextPrice(
        price = minPriceOnTime,
        textMeasurer = textMeasurer,
        offsetY = minPriceOffsetY
    )
}

private fun DrawScope.drawDashedLine(
    color: Color = Color.White,
    start: Offset,
    end: Offset,
    strokeWidth: Float = 1f
) {
    drawLine(
        color = color,
        start = start,
        end = end,
        strokeWidth = strokeWidth,
        pathEffect = PathEffect.dashPathEffect(
            intervals = floatArrayOf(
                4.dp.toPx(), 4.dp.toPx()
            )
        )
    )
}

private fun DrawScope.drawTextPrice(
    price: Float,
    textMeasurer: TextMeasurer,
    offsetY: Float,
    fontSize: TextUnit = 12.sp,
    fontWeight: FontWeight = FontWeight.Bold,
    color: Color = Color.White
) {
    val textLayoutResult = textMeasurer.measure(
        text = price.toString(),
        style = TextStyle(
            fontSize = fontSize,
            fontWeight = fontWeight,
            color = color
        )
    )
    drawText(
        textLayoutResult = textLayoutResult,
        topLeft = Offset(x = size.width - textLayoutResult.size.width - 4.dp.toPx(), y = offsetY)
    )
}