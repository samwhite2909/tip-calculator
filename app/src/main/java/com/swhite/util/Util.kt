package com.swhite.util

//Calculates the total tip on the whole bill.
fun calculateTotalTip(totalBill: Double, tipPercentage: Int): Double {
    return if (totalBill > 1 && totalBill.toString().isNotEmpty())
        (totalBill * tipPercentage) / 100
    else 0.0
}

//Calculates the total for the bill + tip per person.
fun calculateTotalPerPerson(
    totalBill: Double,
    splitBy: Int,
    tipPercentage: Int
): Double {
    val bill = calculateTotalTip(
        totalBill = totalBill,
        tipPercentage = tipPercentage
    ) + totalBill
    return (bill / splitBy)
}
