package `in`.unartech.nearbydevs.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Material 3 Expressive — generous rounded corners.
val NbdShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

object NbdRadius {
    val xs = 8.dp
    val sm = 12.dp
    val md = 18.dp
    val lg = 24.dp
    val xl = 28.dp
    val xxl = 36.dp
    val pill = 999.dp
}
