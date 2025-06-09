package com.newstudio.ujkhataapp.ui.component

import android.os.Build
import androidx.annotation.RequiresApi
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
import com.newstudio.ujkhataapp.data.model.EntryInEntry
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EntryInEntryCard(
    entry: EntryInEntry,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    onCrossToggle: (Boolean) -> Unit = {},
) {
    val zone = ZoneId.systemDefault()
    val currentDate = LocalDate.now()
    val entryDate = Instant.ofEpochMilli(entry.time).atZone(zone).toLocalDate()

    val fullMonthsPassed = ChronoUnit.MONTHS.between(entryDate, currentDate).toInt()
    val dateAfterFullMonths = entryDate.plusMonths(fullMonthsPassed.toLong())
    val extraDays = ChronoUnit.DAYS.between(dateAfterFullMonths, currentDate).toInt()

    val monthlyRate = 3.0 / 100.0
    val dailyRate = monthlyRate / 30.0

    val amountAfterMonths = if (fullMonthsPassed < 1) {
        entry.amount + (entry.amount * monthlyRate)
    } else {
        var principal = entry.amount
        var monthsLeft = fullMonthsPassed

        while (monthsLeft > 0) {
            val monthsToApply = minOf(monthsLeft, 12)
            val interest = principal * monthlyRate * monthsToApply
            principal += interest
            monthsLeft -= monthsToApply
        }

        principal + (principal * dailyRate * extraDays)
    }

    val interestAmount = amountAfterMonths - entry.amount

    val formattedTime = remember(entry.time) {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(entry.time))
    }

    val textDecoration = if (entry.cross) TextDecoration.LineThrough else null

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Top Row: Interest & Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Interest(ब्याज): ₹${"%.2f".format(interestAmount)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF8E44AD),
                    textDecoration = textDecoration
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formattedTime,
                        fontSize = 13.sp,
                        color = Color.Gray,
                        textDecoration = textDecoration
                    )
                    val periodPassed = java.time.Period.between(entryDate, currentDate)
                    val timePassedText = buildString {
                        if (periodPassed.years > 0) append("${periodPassed.years} year${if (periodPassed.years > 1) "s" else ""} ")
                        if (periodPassed.months > 0) append("${periodPassed.months} month${if (periodPassed.months > 1) "s" else ""} ")
                        if (periodPassed.days > 0) append("${periodPassed.days} day${if (periodPassed.days > 1) "s" else ""}")
                    }.trim()
                    if (timePassedText.isNotEmpty()) {
                        Text(
                            text = timePassedText,
                            fontSize = 12.sp,
                            color = Color.DarkGray,
                            textDecoration = textDecoration
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Total
            Text(
                text = "Total(कुल): ₹${"%.2f".format(amountAfterMonths)}",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2C3E50),
                textDecoration = textDecoration
            )


            // Principal Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Principal Icon",
                    tint = Color.Gray,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Principal(मूल्य): ₹${"%.2f".format(entry.amount)}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color.Black,
                    textDecoration = textDecoration
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Row(
                    modifier = Modifier.widthIn(min = 0.dp, max = 200.dp), // Adjust width as needed
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onEdit) {
                        Text("Edit", fontSize = 14.sp)
                    }

                    TextButton(onClick = { onCrossToggle(entry.cross) }) {
                        Text(
                            text = if (entry.cross) "Uncross" else "Cross",
                            color = if (entry.cross) Color.Green else Color.Gray,
                            fontSize = 14.sp
                        )
                    }

                    IconButton(onClick = onDelete) {
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
