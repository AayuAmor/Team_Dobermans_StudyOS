package com.teamdobermans.studyos.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EventNote
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamdobermans.studyos.model.NoteModel
import com.teamdobermans.studyos.ui.components.StudyOSPrimaryButton
import com.teamdobermans.studyos.ui.theme.StudyPurple
import com.teamdobermans.studyos.ui.theme.StudyPurpleLight
import com.teamdobermans.studyos.ui.theme.TextPrimary
import com.teamdobermans.studyos.ui.theme.TextSecondary

@Composable
fun UpcomingReviewsCard(
    notes: List<NoteModel>,
    onNavigateNotes: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (notes.isEmpty()) return

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(StudyPurpleLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.EventNote,
                            contentDescription = null,
                            tint = StudyPurple,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Upcoming Reviews",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
                Surface(shape = RoundedCornerShape(20.dp), color = StudyPurple) {
                    Text(
                        text = "${notes.size}",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val displayed = notes.take(3)
            displayed.forEachIndexed { index, note ->
                UpcomingReviewItem(note = note)
                if (index < displayed.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = Color(0xFFF0EEF9)
                    )
                }
            }

            if (notes.size > 3) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "+ ${notes.size - 3} more",
                    color = StudyPurple,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            StudyOSPrimaryButton(
                text = "Open Notes",
                onClick = onNavigateNotes,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun UpcomingReviewItem(note: NoteModel) {
    val stageLabel = when (note.reviewStage) {
        0 -> "Day 1 review"
        1 -> "Day 4 review"
        2 -> "Day 7 review"
        else -> "Review complete"
    }
    val timeLabel = relativeTimeLabel(note.nextReviewAt)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = note.title.ifBlank { "Untitled Note" },
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(text = stageLabel, fontSize = 11.sp, color = TextSecondary)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Surface(shape = RoundedCornerShape(8.dp), color = StudyPurpleLight) {
            Text(
                text = timeLabel,
                fontSize = 10.sp,
                color = StudyPurple,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

private fun relativeTimeLabel(nextReviewAt: Long): String {
    val diff = nextReviewAt - System.currentTimeMillis()
    val days = (diff / (24L * 60 * 60 * 1000)).toInt()
    return when {
        days <= 0 -> "Today"
        days == 1 -> "Tomorrow"
        else -> "In $days days"
    }
}
