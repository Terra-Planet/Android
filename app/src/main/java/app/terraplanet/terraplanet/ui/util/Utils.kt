package app.terraplanet.terraplanet.ui.util

import android.content.Intent
import android.view.KeyEvent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import app.terraplanet.terraplanet.ui.theme.MainColor
import app.terraplanet.terraplanet.ui.theme.colorAware

const val clearStack = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
const val resetStack = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

@Composable
fun LoadingOverlay(color: Color = MainColor) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xBB000000),
    ) {
        Center {
            CircularProgressIndicator(color = color)
        }
    }
}

@Composable
fun Center(content: @Composable () -> Unit) {
    Box(contentAlignment = Alignment.Center) {
        content()
    }
}

@Composable
fun VSpacer(size: Int = 16) {
    Spacer(modifier = Modifier.height(size.dp))
}

@Composable
fun HSpacer(size: Int = 16) {
    Spacer(modifier = Modifier.width(size.dp))
}

@Composable
fun ColumnScope.Expandable() {
    Box(modifier = Modifier.weight(1f))
}

@Composable
fun RowScope.Expandable(content: (@Composable () -> Unit)? = null) {
    Box(modifier = Modifier.weight(1f)) { content?.invoke() }
}

@Composable
fun Container(
    modifier: Modifier,
    shape: Shape = RectangleShape,
    contentColor: Color = contentColorFor(Color.Transparent),
    border: BorderStroke? = null,
    elevation: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = Color.Transparent,
        contentColor = contentColor,
        border = border,
        elevation = elevation
    ) {
        content()
    }
}

@Composable
fun SimpleTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isBold: Boolean = false,
    fontSize: TextUnit = TextUnit.Unspecified,
    onDone: () -> Unit
) {
    BoxWithConstraints(
        modifier = modifier.clipToBounds()
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .requiredWidth(maxWidth + 16.dp)
                .offset(x = (-8).dp)
                .onKeyEvent {
                    if (it.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
                        onDone()
                        return@onKeyEvent true
                    }
                    false
                },
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                textColor = colorAware(),
            ),
            textStyle = TextStyle(
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                fontSize = fontSize
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onDone() })
        )
    }
}
