package com.newstudio.ujkhataapp.ui.component

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.newstudio.ujkhataapp.data.model.Entry
import com.newstudio.ujkhataapp.data.model.EntryInEntry
import com.newstudio.ujkhataapp.ui.screen.calculateInterestFromEntryInEntry
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EntryCard(
    entry: Entry,
    entryInEntries: List<EntryInEntry> = emptyList(), // ✅ Only these matter for calculation
    onClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onCrossToggle: (isCross: Boolean) -> Unit = {},
) {
    val zone = ZoneId.systemDefault()
    val currentDate = LocalDate.now()

    fun calculateWithInterest(amount: Double, time: Long, interestRate: Double): Double {
        val entryDate = Instant.ofEpochMilli(time).atZone(zone).toLocalDate()
        val fullMonths = ChronoUnit.MONTHS.between(entryDate, currentDate).toInt()
        val dateAfterFullMonths = entryDate.plusMonths(fullMonths.toLong())
        val extraDays = ChronoUnit.DAYS.between(dateAfterFullMonths, currentDate).toInt()

        val monthlyRate = interestRate / 100.0
        val dailyRate = monthlyRate / 30.0

        return if (fullMonths < 1) {
            amount + (amount * monthlyRate)
        } else {
            var principal = amount
            var monthsLeft = fullMonths

            while (monthsLeft > 0) {
                val monthsToApply = minOf(monthsLeft, 12)
                val interest = principal * monthlyRate * monthsToApply
                principal += interest
                monthsLeft -= monthsToApply
            }

            principal + (principal * dailyRate * extraDays)
        }
    }

    // ✅ Sum child entry principal and interest
    val totalPrincipal = entryInEntries.sumOf { if(!it.cross) it.amount else 0.0 }
    val totalInterest = entryInEntries.sumOf { if(!it.cross)calculateInterestFromEntryInEntry(it, it.interestRate) else 0.0 }
    val totalAmount = entryInEntries.sumOf { if(!it.cross) it.amount else 0.0 } + entryInEntries.sumOf { if(!it.cross)calculateInterestFromEntryInEntry(it, it.interestRate) else 0.0 }

    val formattedTime = remember(entry.time) {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(entry.time))
    }

    val periodPassed = Period.between(
        Instant.ofEpochMilli(entry.time).atZone(zone).toLocalDate(),
        currentDate
    )
    val timePassedText = buildString {
        if (periodPassed.years > 0) append("${periodPassed.years} year${if (periodPassed.years > 1) "s" else ""} ")
        if (periodPassed.months > 0) append("${periodPassed.months} month${if (periodPassed.months > 1) "s" else ""} ")
        if (periodPassed.days > 0) append("${periodPassed.days} day${if (periodPassed.days > 1) "s" else ""}")
    }.trim()

    val textDecoration = if (entry.cross) TextDecoration.LineThrough else null

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Interest(ब्याज): ₹${"%.2f".format(totalInterest)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF8E44AD),
                    textDecoration = textDecoration
                )

            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Total(कुल): ₹${"%.2f".format(totalAmount)}",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2C3E50),
                textDecoration = textDecoration
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Principal Icon",
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Principal(मूल्य): ₹${"%.2f".format(totalPrincipal)}",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color.Black,
                            textDecoration = textDecoration
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        modifier = Modifier.fillMaxWidth(0.55f),
                        text = "Jewellery: ${entry.jewellery}",
                        fontSize = 13.sp,
                        color = Color(0xFF34495E),
                        textDecoration = textDecoration
                    )
                }

                Row {
                    TextButton(onClick = onEditClick) {
                        Text("Edit", fontSize = 14.sp)
                    }

                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier
                            .size(32.dp)
                            .padding(top = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.Red
                        )
                    }
                }
            }
        }
    }
}
