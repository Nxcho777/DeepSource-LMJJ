
package cl.duoc.deepsource.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.deepsource.data.mock.MockData

private val CreateBackground = Color(0xFF080D1C)
private val CreateCard = Color(0xFF101A30)
private val CreateTeal = Color(0xFF29B5A6)
private val CreateText = Color(0xFFF3F6FF)
private val CreateMuted = Color(0xFFA8B5CC)

@Composable
fun CreateProjectScreen(
    onBack: () -> Unit,
    onProjectCreated: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var client by remember { mutableStateOf("") }
    var hours by remember { mutableStateOf("40") }
    var selectedIcon by remember { mutableStateOf(MockData.availableIcons.first()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = CreateTeal,
        unfocusedBorderColor = Color(0xFF34415B),
        focusedTextColor = CreateText,
        unfocusedTextColor = CreateText,
        focusedLabelColor = CreateTeal,
        unfocusedLabelColor = CreateMuted,
        cursorColor = CreateTeal
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreateBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = CreateText
                )
            }

            Spacer(Modifier.width(8.dp))

            Column {
                Text(
                    text = "Crear proyecto",
                    color = CreateText,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Configura un nuevo proyecto",
                    color = CreateMuted,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(Modifier.height(26.dp))

        Text(
            text = "Información del proyecto",
            color = CreateText,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(14.dp))

        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                errorMessage = null
            },
            label = { Text("Nombre del proyecto") },
            placeholder = { Text("Ej. Proyecto Atlas") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = fieldColors
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = client,
            onValueChange = {
                client = it
                errorMessage = null
            },
            label = { Text("Cliente") },
            placeholder = { Text("Ej. Deep Source") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = fieldColors
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = description,
            onValueChange = {
                description = it
                errorMessage = null
            },
            label = { Text("Descripción o categoría") },
            placeholder = { Text("Ej. Producto, Web, Mobile") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = fieldColors
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = hours,
            onValueChange = {
                hours = it.filter(Char::isDigit)
                errorMessage = null
            },
            label = { Text("Horas estimadas") },
            placeholder = { Text("40") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = fieldColors
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Elige un icono",
            color = CreateText,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MockData.availableIcons.forEach { iconRes ->
                val isSelected = selectedIcon == iconRes

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(76.dp)
                        .background(
                            color = if (isSelected) Color(0xFF183B43) else CreateCard,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) CreateTeal else Color(0xFF34415B),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { selectedIcon = iconRes },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = iconRes),
                        contentDescription = "Icono de proyecto",
                        modifier = Modifier.size(38.dp)
                    )

                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Icono seleccionado",
                            tint = CreateTeal,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(5.dp)
                                .size(15.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(18.dp))

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = Color(0xFFFF7777),
                fontSize = 13.sp
            )
            Spacer(Modifier.height(8.dp))
        }

        Button(
            onClick = {
                val estimatedHours = hours.toIntOrNull()

                when {
                    name.isBlank() ->
                        errorMessage = "Ingresa el nombre del proyecto."
                    client.isBlank() ->
                        errorMessage = "Ingresa el nombre del cliente."
                    description.isBlank() ->
                        errorMessage = "Ingresa una descripción o categoría."
                    estimatedHours == null || estimatedHours <= 0 ->
                        errorMessage = "Las horas estimadas deben ser mayores que cero."
                    else -> {
                        try {
                            MockData.addProject(
                                name = name.trim(),
                                description = description.trim(),
                                client = client.trim(),
                                totalMinutes = estimatedHours * 60,
                                iconRes = selectedIcon
                            )
                            onProjectCreated()
                        } catch (e: IllegalArgumentException) {
                            errorMessage = e.message ?: "No se pudo crear el proyecto."
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CreateTeal,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Crear proyecto",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}
