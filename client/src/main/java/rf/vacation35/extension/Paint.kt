package rf.vacation35.extension

import android.graphics.Paint
import android.graphics.Rect

fun Paint.getTextBounds(obj: Any, rect: Rect = Rect()): Rect {
    val text = obj.toString()
    getTextBounds(text, 0, text.length, rect)
    return rect
}
