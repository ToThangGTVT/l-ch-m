import android.widget.RemoteViews
import android.content.res.ColorStateList

fun test(views: RemoteViews) {
    views.setColorStateList(android.R.id.text1, "setBackgroundTintList", ColorStateList.valueOf(0xFFFFFFFF.toInt()))
}
