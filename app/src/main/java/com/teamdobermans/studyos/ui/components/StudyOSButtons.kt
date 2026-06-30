package com.teamdobermans.studyos.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teamdobermans.studyos.ui.theme.PriorityHigh
import com.teamdobermans.studyos.ui.theme.StudyPurple
import com.teamdobermans.studyos.ui.theme.StudyPurpleLight

private val StudyOSButtonShape = RoundedCornerShape(24.dp)
private val StudyOSButtonMinHeight = 52.dp

@Composable
fun StudyOSPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    isLoading: Boolean = false,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding
) {
    Button(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = StudyOSButtonMinHeight),
        enabled = true,
        shape = StudyOSButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = StudyPurple,
            contentColor = Color.White,
            disabledContainerColor = StudyPurple,
            disabledContentColor = Color.White
        ),
        contentPadding = contentPadding
    ) {
        StudyOSButtonContent(text, isLoading, leadingIcon, trailingIcon, Color.White)
    }
}

@Composable
fun StudyOSSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    isLoading: Boolean = false,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding
) {
    Button(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = StudyOSButtonMinHeight),
        enabled = true,
        shape = StudyOSButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = StudyPurpleLight,
            contentColor = StudyPurple,
            disabledContainerColor = StudyPurpleLight,
            disabledContentColor = StudyPurple
        ),
        contentPadding = contentPadding
    ) {
        StudyOSButtonContent(text, isLoading, leadingIcon, trailingIcon, StudyPurple)
    }
}

@Composable
fun StudyOSOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    isLoading: Boolean = false,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = StudyOSButtonMinHeight),
        enabled = true,
        shape = StudyOSButtonShape,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = StudyPurple,
            disabledContainerColor = Color.White,
            disabledContentColor = StudyPurple
        ),
        border = BorderStroke(1.5.dp, StudyPurple),
        contentPadding = contentPadding
    ) {
        StudyOSButtonContent(text, isLoading, leadingIcon, trailingIcon, StudyPurple)
    }
}

@Composable
fun StudyOSTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    isLoading: Boolean = false,
    contentPadding: PaddingValues = ButtonDefaults.TextButtonContentPadding
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = 44.dp),
        enabled = true,
        colors = ButtonDefaults.textButtonColors(
            contentColor = StudyPurple,
            disabledContentColor = StudyPurple
        ),
        contentPadding = contentPadding
    ) {
        StudyOSButtonContent(text, isLoading, leadingIcon, trailingIcon, StudyPurple)
    }
}

@Composable
fun StudyOSGoogleButton(
    text: String = "Continue with Google",
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    isLoading: Boolean = false,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = StudyOSButtonMinHeight),
        enabled = true,
        shape = StudyOSButtonShape,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = StudyPurple,
            disabledContainerColor = Color.White,
            disabledContentColor = StudyPurple
        ),
        border = BorderStroke(1.5.dp, StudyPurple),
        contentPadding = contentPadding
    ) {
        StudyOSButtonContent(text, isLoading, leadingIcon, null, StudyPurple)
    }
}

@Composable
fun StudyOSIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = StudyPurpleLight,
    contentColor: Color = StudyPurple
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = true,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor,
            disabledContentColor = contentColor
        )
    ) {
        Icon(icon, contentDescription = contentDescription)
    }
}

@Composable
fun StudyOSDestructiveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    isLoading: Boolean = false,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding
) {
    Button(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = StudyOSButtonMinHeight),
        enabled = true,
        shape = StudyOSButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = PriorityHigh,
            contentColor = Color.White,
            disabledContainerColor = PriorityHigh,
            disabledContentColor = Color.White
        ),
        contentPadding = contentPadding
    ) {
        StudyOSButtonContent(text, isLoading, leadingIcon, trailingIcon, Color.White)
    }
}

@Composable
fun StudyOSLoadingButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding
) = StudyOSPrimaryButton(
    text = text,
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    leadingIcon = leadingIcon,
    trailingIcon = trailingIcon,
    isLoading = isLoading,
    contentPadding = contentPadding
)

@Composable
private fun StudyOSButtonContent(
    text: String,
    isLoading: Boolean,
    leadingIcon: ImageVector?,
    trailingIcon: ImageVector?,
    progressColor: Color
) {
    if (isLoading) {
        CircularProgressIndicator(
            modifier = Modifier.size(18.dp),
            strokeWidth = 2.dp,
            color = progressColor
        )
        Spacer(modifier = Modifier.width(10.dp))
    } else if (leadingIcon != null) {
        Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
    }
    Text(text, fontWeight = FontWeight.SemiBold)
    if (!isLoading && trailingIcon != null) {
        Spacer(modifier = Modifier.width(8.dp))
        Icon(trailingIcon, contentDescription = null, modifier = Modifier.size(18.dp))
    }
}
