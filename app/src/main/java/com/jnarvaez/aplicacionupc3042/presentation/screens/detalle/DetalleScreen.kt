package com.jnarvaez.aplicacionupc3042.presentation.screens.detalle

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jnarvaez.aplicacionupc3042.R
import com.jnarvaez.aplicacionupc3042.presentation.common.CuadroInfo
import com.jnarvaez.aplicacionupc3042.presentation.common.InfoCard
import com.jnarvaez.aplicacionupc3042.presentation.common.MetaBadge


@Composable
fun DetalleScreen(){

    val colorAzulMedio = Color(0xFF3823AB)
    val colorAzulOscuro = Color(0xFF101140)
    val colorGris = Color(0xFF7D7F99)
    val colorGrisOscuro = Color(0xFF555B6E)
    val colorVerde = Color(0xFF00A86B)
    val colorRojo = Color(0xFFB3261E)
    val colorFondo = Color(0xFFF7F7FD)

    Column(
        modifier = Modifier.fillMaxSize().background(colorFondo).padding(all = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        //1. Imagen
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            modifier = Modifier.size(width = 180.dp, height = 240.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.librook),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        //2. Cuadroinfo
        CuadroInfo(Icons.Default.Book,"COLECCION CLASICA")
        Spacer(modifier = Modifier.height(12.dp))

        //3. Titulo y el autor
        Text(
            text = "Cien Años de Soledad",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            color = colorAzulOscuro
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = "",
                tint = colorAzulMedio,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Gabriel García Márquez",
                fontSize = 14.sp,
                color = colorGrisOscuro,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        //4. Tarjetas InfoCard
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoCard(
                icon = Icons.Outlined.DateRange,
                label = "Publicación",
                value = "1967",
                modifier = Modifier.weight(1f)
            )
            InfoCard(
                icon = Icons.AutoMirrored.Outlined.Label,
                label = "Género",
                value = "Realismo M...",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        //5. Barra de disponibilidad
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF3F2FE),
            border = BorderStroke(1.dp, Color(0xFFE2E0FD)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(8.dp).background((colorVerde), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Disponibilidad en biblioteca",
                        fontSize = 11.sp,
                        color = colorGrisOscuro
                    )
                }
                Text(
                    text = "Copia Física • Estante 4B",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorAzulMedio
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        //6. Sinopsis
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFEAEAF5)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header Descripción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Menu,
                            contentDescription = null,
                            tint = colorAzulOscuro,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Descripción",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorAzulOscuro
                        )
                    }
                    Text(
                        text = "SINOPSIS OFICIAL",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorGris
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Texto Justificado/Largo
                Text(
                    text = "La monumental obra de Gabriel García Márquez narra las siete generaciones de la estirpe Buendía en la mítica aldea de Macondo. Con una maestría estilística inigualable, la novela entrelaza lo fantástico con la realidad sociopolítica de América Latina, explorando la soledad, el destino circular y los amores prohibidos que marcan para siempre la memoria cultural hispanoamericana.",
                    fontSize = 12.sp,
                    color = colorGris,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Justify
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Metadata Chips
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetaBadge(text = "ISBN: 978-0307474728")
                    MetaBadge(text = "496 Páginas")
                }
                Spacer(modifier = Modifier.height(6.dp))
                MetaBadge(text = "Idioma: Español")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        //7. Botones
        Button(
            onClick = {},
            colors = ButtonDefaults.buttonColors(containerColor = colorAzulMedio),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
        ) {
            Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Editar", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFDCDC)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
        ) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = colorRojo,
                modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Eliminar", color = colorRojo, fontWeight = FontWeight.Bold)
        }
    }
}