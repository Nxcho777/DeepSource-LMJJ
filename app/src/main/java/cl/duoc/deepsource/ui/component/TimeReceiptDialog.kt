package cl.duoc.deepsource.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import cl.duoc.deepsource.ui.animation.motionDurationMillis
import cl.duoc.deepsource.ui.viewmodel.TimeReceipt
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val receiptText = Color(0xFFF7FAFF)
private val receiptMuted = Color(0xFFAEB9D0)
private val receiptTeal = Color(0xFF42C7B8)

@Composable
fun TimeReceiptDialog(receipt: TimeReceipt, onAccept: () -> Unit) {
    val duration = motionDurationMillis(220)
    val visible = remember(receipt.entryId) { MutableTransitionState(false).apply { targetState = true } }
    var closing by remember(receipt.entryId) { mutableStateOf(false) }
    LaunchedEffect(closing, visible.isIdle, visible.currentState) {
        if (closing && visible.isIdle && !visible.currentState) onAccept()
    }
    val dateLabel = remember(receipt.date) {
        LocalDate.parse(receipt.date).format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("es", "CL")))
    }

    Dialog(onDismissRequest = {}, properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)) {
        AnimatedVisibility(
            visibleState = visible,
            enter = fadeIn(tween(duration)) + scaleIn(initialScale = .96f, animationSpec = tween(duration)),
            exit = fadeOut(tween(duration)) + scaleOut(targetScale = .96f, animationSpec = tween(duration))
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth().widthIn(max = 440.dp),
                color = Color(0xFF0C1529),
                contentColor = receiptText,
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0x4D42C7B8)),
                shadowElevation = 8.dp
            ) {
                Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("REGISTRO EXITOSO", color = receiptTeal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Comprobante de horas", color = receiptText, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
                    HorizontalDivider(color = Color(0x26FFFFFF))
                    ReceiptRow("Proyecto", receipt.projectName)
                    ReceiptRow("Tarea", receipt.taskName)
                    ReceiptRow("Fecha", dateLabel)
                    ReceiptRow("Duración", receipt.durationLabel)
                    HorizontalDivider(color = Color(0x26FFFFFF))
                    Button(
                        onClick = {
                            if (!closing) {
                                closing = true
                                visible.targetState = false
                            }
                        },
                        enabled = !closing,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2AAE9E), contentColor = Color.White)
                    ) { Text("Aceptar", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@Composable
private fun ReceiptRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = receiptMuted, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text(value, color = receiptText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(2f))
    }
}
