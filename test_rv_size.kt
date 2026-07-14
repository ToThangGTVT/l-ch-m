import android.widget.RemoteViews
import android.util.TypedValue

fun test(views: RemoteViews) {
    views.setTextViewTextSize(android.R.id.text1, TypedValue.COMPLEX_UNIT_SP, 14f)
}
