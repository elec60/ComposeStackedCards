package mousavi.hashem.composestackedcards

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import mousavi.hashem.composestackedcards.ui.theme.ComposeStackedCardsTheme
import kotlin.random.Random

@Composable
fun MainScreen(
    cardsCount: Int = 4
) {
    val colors = generateColors(cardsCount)
    val offsetValue = cardsCount * 18

    // Track the index of the top card
    var topIndex by remember {
        mutableIntStateOf(0)
    }
    val coroutineScope = rememberCoroutineScope()
    val animatable = remember {
        Animatable(initialValue = 0f)
    }

    // Z-indexes for overlapping cards
    val zIndexes = remember {
        mutableStateListOf<Float>().apply {
            repeat(cardsCount) { index ->
                add(-index.toFloat())
            }
        }
    }

    // Offsets for positioning cards
    val offsets = remember {
        mutableStateListOf<IntOffset>().apply {
            repeat(cardsCount) { index ->
                add(
                    IntOffset(
                        x = index * offsetValue,
                        y = index * offsetValue / 2
                    )
                )
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        repeat(cardsCount) { index ->
            val isTopCard = index == topIndex
            MyCard(
                modifier = Modifier
                    .offset {
                        offsets[index]
                    }
                    .offset {
                        if (isTopCard) {
                            // Offset for the top card when selected
                            IntOffset(
                                x = ((cardsCount - 1) * offsetValue * animatable.value).toInt(),
                                y = ((cardsCount - 1) * offsetValue * animatable.value.toInt()) / 2
                            )
                        } else {
                            // Offset for other cards
                            IntOffset(
                                x = -(offsetValue * animatable.value).toInt(),
                                y = -(offsetValue * animatable.value).toInt() / 2
                            )
                        }

                    }
                    .graphicsLayer {
                        // Apply rotation for the top card
                        rotationZ = if (index == topIndex) animatable.value * 15 else 0f
                    }
                    .pointerInput(Unit) {
                        this.awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()

                                when (event.type) {
                                    PointerEventType.Press -> {
                                        // Allow only the top card to be pressed
                                        if (index != topIndex || animatable.isRunning) continue
                                        topIndex = index
                                    }

                                    PointerEventType.Release -> {
                                        if (index != topIndex || animatable.isRunning) continue
                                        // Animate card movement on release
                                        coroutineScope.launch {
                                            animatable.animateTo(
                                                1f,
                                                animationSpec = tween(durationMillis = 500)
                                            )
                                            rotateList(zIndexes)
                                            rotateList(offsets)
                                            animatable.snapTo(0f)
                                            topIndex += 1
                                            topIndex %= cardsCount
                                        }
                                    }
                                }
                            }
                        }
                    }
                    .zIndex(zIndexes[index])
                    .fillMaxWidth(0.4f)
                    .aspectRatio(0.8f)
                    .shadow(elevation = 10.dp),
                color = colors[index],
                index = index
            )
        }
    }

}

/**
 * Function to rotate elements of a mutable list.
 *
 * @param list The list to be rotated.
 */
fun <T> rotateList(list: MutableList<T>) {
    if (list.size <= 1) return
    val lastElement = list.removeAt(list.size - 1)
    list.add(0, lastElement)
}

/**
 * Generates a list of random colors.
 *
 * @param count The number of colors to generate.
 * @return List of randomly generated colors.
 */
@Composable
private fun generateColors(count: Int) = remember {
    List(count) { randomColor() }
}

@Composable
fun MyCard(
    modifier: Modifier = Modifier,
    color: Color,
    index: Int,
) {
    Box(
        modifier = modifier.background(color, shape = RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = index.toString(),
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

fun randomColor(): Color {
    val red = Random.nextInt(256)
    val green = Random.nextInt(256)
    val blue = Random.nextInt(256)
    return Color(red, green, blue)
}

@Preview(showSystemUi = true)
@Composable
fun MainScreenPreview() {
    ComposeStackedCardsTheme {
        MainScreen()
    }
}

@Preview(showSystemUi = true)
@Composable
fun MyCardPreview() {
    ComposeStackedCardsTheme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            MyCard(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .aspectRatio(1f),
                color = Color.Red,
                index = 0
            )
        }
    }
}

