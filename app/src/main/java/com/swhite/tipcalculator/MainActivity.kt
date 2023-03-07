package com.swhite.tipcalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.swhite.tipcalculator.components.InputField
import com.swhite.tipcalculator.ui.theme.TipCalculatorTheme
import com.swhite.tipcalculator.widgets.RoundIconButton
import com.swhite.util.calculateTotalPerPerson
import com.swhite.util.calculateTotalTip

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Application {
                MainContent()
            }
        }
    }
}

//Sets base for the UI of the activity.
@Composable
fun Application(content: @Composable () -> Unit) {
    TipCalculatorTheme() {
        Surface(color = MaterialTheme.colorScheme.background) {
            content()
        }
    }
}

//Creates the top header showing the total per person split.
@Composable
fun TopHeader(totalPerPerson: Double = 0.0) {
    //Creates the background for the header.
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(15.dp)
            .clip(shape = RoundedCornerShape(corner = CornerSize(12.dp))),
        color = Color(0xFFE9D7F7)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            //Formatting the number to always be a double (currency).
            val total = "%.2f".format(totalPerPerson)
            //Creates the total per person text.
            Text(
                text = stringResource(R.string.total_per_person),
                style = MaterialTheme.typography.headlineSmall,
                color = Color.Black
            )
            //Creates the value text for the total amount per person.
            Text(
                text = "£$total",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black
            )
        }
    }
}

//Creates the UI for the activity.
@Composable
fun MainContent() {
    //State variables.
    val splitByState = remember {
        mutableStateOf(1)
    }

    val tipAmountState = remember {
        mutableStateOf(0.0)
    }

    val totalPerPersonState = remember {
        mutableStateOf(0.0)
    }

    val range = IntRange(start = 1, endInclusive = 100)

    //Creates the form for the bill and tip fields.
    BillForm(
        splitByState = splitByState,
        tipAmountState = tipAmountState,
        totalPerPersonState = totalPerPersonState
    )
}

//Creates the main form for the bill and tip fields.
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BillForm(
    modifier: Modifier = Modifier,
    range: IntRange = 1..100,
    splitByState: MutableState<Int>,
    tipAmountState: MutableState<Double>,
    totalPerPersonState: MutableState<Double>,
    onValChange: (String) -> Unit = {}
) {
    //State variables.
    val totalBillState = remember {
        mutableStateOf("")
    }

    val validState = remember(totalBillState.value) {
        totalBillState.value.trim().isNotEmpty()
    }

    val keyboardController = LocalSoftwareKeyboardController.current

    val sliderPositionState = remember {
        mutableStateOf(0f)
    }

    val tipPercentage = (sliderPositionState.value * 100).toInt()

    Column(
        modifier = Modifier.padding(10.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        //Adds in the header.
        TopHeader(totalPerPerson = totalPerPersonState.value)

        //Adds a background for the rest of the UI.
        Surface(
            modifier = modifier
                .padding(2.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(corner = CornerSize(8.dp)),
            border = BorderStroke(width = 1.dp, color = Color.LightGray)
        ) {
            Column(
                modifier = modifier.padding(6.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                //Creates the bill text input.
                InputField(
                    valueState = totalBillState,
                    labelId = stringResource(R.string.enter_bill),
                    enabled = true,
                    isSingleLine = true,
                    onAction = KeyboardActions {
                        if (!validState) return@KeyboardActions
                        onValChange(totalBillState.value.trim())

                        keyboardController?.hide()
                    })

                //Checks if we have a bill amount and displays the rest of the UI if we do.
                if (validState) {
                    Row(
                        modifier = modifier.padding(3.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        //Creates the split text.
                        Text(
                            text = stringResource(R.string.split),
                            modifier = modifier.align(alignment = Alignment.CenterVertically)
                        )
                        Spacer(modifier = modifier.width(120.dp))
                        Row(
                            modifier = modifier.padding(horizontal = 3.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            //Creates the minus button.
                            RoundIconButton(
                                imageVector = Icons.Default.Remove,
                                onClick = {
                                    //If we have more than one person, remove one.
                                    splitByState.value =
                                        if (splitByState.value > 1) splitByState.value - 1
                                        else 1
                                    //Alter the total per person accordingly.
                                    totalPerPersonState.value =
                                        calculateTotalPerPerson(
                                            totalBill = totalBillState.value.toDouble(),
                                            splitBy = splitByState.value,
                                            tipPercentage = tipPercentage
                                        )
                                }
                            )
                            //Creates the value for the text displaying the number of people in the split.
                            Text(
                                text = "${splitByState.value}",
                                modifier = modifier
                                    .align(Alignment.CenterVertically)
                                    .padding(
                                        start = 9.dp,
                                        end = 9.dp
                                    )
                            )
                            RoundIconButton(
                                imageVector = Icons.Default.Add,
                                onClick = {
                                    //If we are within the range of people, add one.
                                    if (splitByState.value < range.last) {
                                        splitByState.value = splitByState.value + 1
                                    }

                                    //Alter the total per person accordingly.
                                    totalPerPersonState.value =
                                        calculateTotalPerPerson(
                                            totalBill = totalBillState.value.toDouble(),
                                            splitBy = splitByState.value,
                                            tipPercentage = tipPercentage
                                        )
                                }
                            )
                        }
                    }
                    Row(
                        modifier = modifier.padding(
                            horizontal = 3.dp,
                            vertical = 12.dp
                        )
                    ) {
                        //Creates the tip text.
                        Text(
                            text = stringResource(R.string.tip),
                            modifier = modifier.align(alignment = Alignment.CenterVertically)
                        )
                        Spacer(modifier = modifier.width(200.dp))

                        //Creates the value of the tip amount.
                        Text(
                            text = "£${tipAmountState.value}",
                            modifier = modifier.align(alignment = Alignment.CenterVertically)
                        )

                    }
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        //Creates the text for the tip percentage, adjusted to by the slider.
                        Text(text = "${tipPercentage}%")
                        Spacer(modifier = modifier.height(14.dp))

                        //Allows the user to adjust the value of the tip between 0 - 100%.
                        Slider(
                            value = sliderPositionState.value,
                            onValueChange = { newVal ->
                                sliderPositionState.value = newVal
                                tipAmountState.value =
                                    calculateTotalTip(
                                        totalBill = totalBillState.value.toDouble(),
                                        tipPercentage = tipPercentage
                                    )
                                totalPerPersonState.value =
                                    calculateTotalPerPerson(
                                        totalBill = totalBillState.value.toDouble(),
                                        splitBy = splitByState.value,
                                        tipPercentage = tipPercentage
                                    )
                            },
                            modifier = modifier.padding(
                                start = 16.dp,
                                end = 16.dp
                            ),
                            steps = 5
                        )
                    }
                } else {
                    Box() {}
                }
            }
        }
    }
}

//Shows preview.
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    TipCalculatorTheme {
        Application {
            MainContent()
        }
    }
}