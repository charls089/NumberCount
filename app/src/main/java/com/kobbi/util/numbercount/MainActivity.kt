package com.kobbi.util.numbercount

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModelProvider
import com.kobbi.util.numbercount.model.CountViewModel
import com.kobbi.util.numbercount.model.NumberViewModel
import com.kobbi.util.numbercount.ui.theme.NumberCountTheme
import com.kobbi.util.numbercount.ui.theme.Typography
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private val mBackPressedCloser by lazy { BackPressedCloser(this) }
    private val viewModel: NumberViewModel =
        ViewModelProvider.NewInstanceFactory().create(NumberViewModel::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            NumberCountTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Column {
                        TopAppBar(
                            elevation = 4.dp,
                            title = {
                                Text("숫자 카운트")
                            },
                            backgroundColor = MaterialTheme.colors.primarySurface,
                            actions = {
                                AnimatedVisibility(visible = !viewModel.editMode.value) {
                                    IconButton(onClick = { viewModel.add(0) }) {
                                        Icon(Icons.Filled.Add, null)
                                    }
                                }
                                AnimatedVisibility(visible = viewModel.counts.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.setEditMode() }) {
                                        Icon(
                                            if (viewModel.editMode.value)
                                                Icons.Filled.Check
                                            else
                                                Icons.Filled.Edit,
                                            null
                                        )
                                    }
                                }
                            })
                        CountColumn(viewModel)
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        if (viewModel.editMode.value)
            viewModel.setEditMode()
        else
            mBackPressedCloser.onBackPressed()
    }
}

@Composable
fun CountColumn(numberVm: NumberViewModel) {
    val counts = numberVm.counts
    val editMode = numberVm.editMode
    Column(modifier = Modifier.fillMaxHeight()) {
        AnimatedVisibility(visible = editMode.value) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(8.dp, 12.dp)
                    .clickable {
                        numberVm.checkedAll(!numberVm.isCheckedAll())
                    }
            ) {
                Checkbox(checked = numberVm.isCheckedAll(), onCheckedChange = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "전체선택")
            }
        }
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(counts.size) { index ->
                CountCard(counts[index], editMode.value)
            }
        }
        AnimatedVisibility(visible = editMode.value && numberVm.hasChecked()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            ) {
                Divider(
                    modifier = Modifier
                        .height(1.dp)
                        .fillMaxWidth()
                        .shadow(1.dp)
                )
                Row {
                    IconButton(Modifier.weight(1f), "잠금", R.drawable.ic_lock_24dp) {
                        numberVm.lock()
                    }
                    IconButton(Modifier.weight(1f), "잠금해제", R.drawable.ic_lock_open_24dp) {
                        numberVm.unlock()
                    }
                    IconButton(Modifier.weight(1f), "삭제", R.drawable.ic_delete_24dp) {
                        numberVm.remove()
                    }
                }
            }
        }
    }
}

@Composable
fun IconButton(modifier: Modifier, text: String, @DrawableRes iconId: Int, onClick: () -> Unit) {
    Button(
        onClick = { onClick.invoke() },
        modifier = modifier.fillMaxHeight(),
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
        shape = RectangleShape,
        elevation = null
    ) {
        Icon(
            painter = painterResource(id = iconId),
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text, style = Typography.h6)
    }
}

@Composable
fun CountCard(
    countVm: CountViewModel,
    isEditMode: Boolean,
) {
    val count = countVm.count
    val locked = countVm.locked
    val checked = countVm.checked
    val editMode = remember { mutableStateOf(isEditMode) }
    if (isEditMode != editMode.value)
        editMode.value = isEditMode

    Card(
        Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .height(200.dp)
    ) {
        val boxTapped = remember { mutableStateOf(false) }
        val boxInteractionSource = remember { MutableInteractionSource() }
        Box(modifier = Modifier.run {
            if (editMode.value || locked.value) {
                indication(boxInteractionSource, LocalIndication.current)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = { offset ->
                                boxTapped.value = true
                                val press = PressInteraction.Press(offset)
                                boxInteractionSource.emit(press)
                                tryAwaitRelease()
                                boxInteractionSource.emit(PressInteraction.Release(press))
                                boxTapped.value = false
                            },
                            onTap = {
                                if (editMode.value)
                                    countVm.setChecked(!checked.value)
                            },
                            onLongPress = {
                                if (!editMode.value && locked.value)
                                    countVm.setLocked(false)
                            }
                        )
                    }
            } else {
                this
            }
        }) {
            AnimatedVisibility(
                visible = editMode.value,
                modifier = Modifier.align(Alignment.TopStart),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Checkbox(
                    checked = checked.value,
                    onCheckedChange = null,
                    modifier = Modifier.padding(8.dp)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                AnimatedVisibility(
                    visible = !editMode.value && !locked.value,
                    modifier = Modifier.weight(1f)
                ) {
                    val tapped = remember { mutableStateOf(false) }
                    val interactionSource = remember { MutableInteractionSource() }
                    Image(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RectangleShape)
                            .indication(interactionSource, LocalIndication.current)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = { offset ->
                                        tapped.value = true
                                        val press = PressInteraction.Press(offset)
                                        interactionSource.emit(press)
                                        tryAwaitRelease()
                                        interactionSource.emit(PressInteraction.Release(press))
                                        tapped.value = false
                                    },
                                    onTap = { countVm.minus() },
                                    onLongPress = { countVm.reset() }
                                )
                            },
                        painter = painterResource(id = R.drawable.ic_remove_48dp),
                        contentScale = ContentScale.None,
                        contentDescription = "minus",
                        colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurface)
                    )
                }
                val showDialog = remember { mutableStateOf(false) }
                if (showDialog.value) {
                    CountEditDialog(count.value) {
                        countVm.setCount(it)
                        showDialog.value = false
                    }
                }
                Box(
                    modifier = Modifier
                        .widthIn(100.dp, 200.dp)
                        .fillMaxHeight()
                        .run {
                            if (!editMode.value && !locked.value)
                                pointerInput(Unit) {
                                    detectTapGestures(onLongPress = {
                                        showDialog.value = true
                                    })
                                }
                            else
                                this
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = String.format("%,d", count.value),
                        style = Typography.h3,
                    )
                }
                AnimatedVisibility(
                    visible = !editMode.value && !locked.value,
                    modifier = Modifier.weight(1f)
                ) {
                    Button(
                        onClick = { countVm.plus() },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
                        shape = RectangleShape,
                        elevation = null
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_add_48dp),
                            contentDescription = "plus",
                            colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurface)
                        )
                    }
                }
            }
            AnimatedVisibility(
                visible = locked.value,
                modifier = Modifier.align(Alignment.TopEnd),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Icon(
                    modifier = Modifier.padding(8.dp),
                    painter = painterResource(id = R.drawable.ic_lock_40dp),
                    tint = MaterialTheme.colors.onSurface,
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
fun CountEditDialog(count: Long, onValueChanged: (String) -> Unit) {
    val inputValue = remember { mutableStateOf(TextFieldValue(count.toString())) }
    val focusRequester = remember { FocusRequester() }
    Dialog(onDismissRequest = {}) {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colors.surface,
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = "변경할 숫자를 입력해주세요.",
                    style = Typography.h5,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(
                    modifier = Modifier.height(16.dp)
                )
                BasicTextField(
                    value = inputValue.value,
                    onValueChange = { inputValue.value = it },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Done
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colors.onSurface),
                    singleLine = true,
                    textStyle = Typography.h3.copy(
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colors.onSurface
                    ),
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) {
                                val text = inputValue.value.text
                                inputValue.value = inputValue.value.copy(
                                    selection = TextRange(0, text.length)
                                )
                            }
                        }
                )
                Spacer(
                    modifier = Modifier.height(16.dp)
                )
                Row(modifier = Modifier.height(48.dp)) {
                    Button(
                        onClick = { onValueChanged.invoke(count.toString()) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    ) {
                        Text(
                            text = "취소",
                            style = Typography.h6
                        )
                    }
                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )
                    Button(
                        onClick = { onValueChanged.invoke(inputValue.value.text) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        Text(
                            text = "확인",
                            style = Typography.h6
                        )
                    }
                }
            }
        }
    }
    LaunchedEffect(Unit) {
        delay(300)
        focusRequester.requestFocus()
    }
}