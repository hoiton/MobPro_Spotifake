import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

public val FolderIcon: ImageVector
    get() {
        if (_folderIcon != null) {
            return _folderIcon!!
        }
        _folderIcon = ImageVector.Builder(
            name = "Folder2Open",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 16f,
            viewportHeight = 16f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)),
                fillAlpha = 1.0f,
                stroke = null,
                strokeAlpha = 1.0f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(1f, 3.5f)
                arcTo(1.5f, 1.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 2.5f, 2f)
                horizontalLineToRelative(2.764f)
                curveToRelative(0.958f, 0f, 1.76f, 0.56f, 2.311f, 1.184f)
                curveTo(7.985f, 3.648f, 8.48f, 4f, 9f, 4f)
                horizontalLineToRelative(4.5f)
                arcTo(1.5f, 1.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 15f, 5.5f)
                verticalLineToRelative(0.64f)
                curveToRelative(0.57f, 0.265f, 0.94f, 0.876f, 0.856f, 1.546f)
                lineToRelative(-0.64f, 5.124f)
                arcTo(2.5f, 2.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 12.733f, 15f)
                horizontalLineTo(3.266f)
                arcToRelative(2.5f, 2.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, -2.481f, -2.19f)
                lineToRelative(-0.64f, -5.124f)
                arcTo(1.5f, 1.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 1f, 6.14f)
                close()
                moveTo(2f, 6f)
                horizontalLineToRelative(12f)
                verticalLineToRelative(-0.5f)
                arcToRelative(0.5f, 0.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.5f, -0.5f)
                horizontalLineTo(9f)
                curveToRelative(-0.964f, 0f, -1.71f, -0.629f, -2.174f, -1.154f)
                curveTo(6.374f, 3.334f, 5.82f, 3f, 5.264f, 3f)
                horizontalLineTo(2.5f)
                arcToRelative(0.5f, 0.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.5f, 0.5f)
                close()
                moveToRelative(-0.367f, 1f)
                arcToRelative(0.5f, 0.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.496f, 0.562f)
                lineToRelative(0.64f, 5.124f)
                arcTo(1.5f, 1.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, 3.266f, 14f)
                horizontalLineToRelative(9.468f)
                arcToRelative(1.5f, 1.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, 1.489f, -1.314f)
                lineToRelative(0.64f, -5.124f)
                arcTo(0.5f, 0.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, 14.367f, 7f)
                close()
            }
        }.build()
        return _folderIcon!!
    }

private var _folderIcon: ImageVector? = null
