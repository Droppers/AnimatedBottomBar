package nl.joery.animatedbottombar.utils

import android.content.Context
import android.view.MenuInflater
import android.widget.PopupMenu
import androidx.annotation.MenuRes
import androidx.core.view.iterator
import nl.joery.animatedbottombar.AnimatedBottomBar
import nl.joery.animatedbottombar.NoCopyArrayList


internal object MenuParser {
    fun parse(context: Context, @MenuRes resId: Int, exception: Boolean): Array<out AnimatedBottomBar.Tab> {
        val p = PopupMenu(context, null)
        MenuInflater(context).inflate(resId, p.menu)
        val menu = p.menu

        val size = menu.size()
        return Array(size) { i ->
            val item = menu.getItem(i)
            if (exception) {
                if (item.title == null) {
                    throw Exception("Menu item attribute 'title' is missing")
                }

                if (item.icon == null) {
                    throw Exception("Menu item attribute 'icon' for tab named '${item.title}' is missing")
                }
            }

            AnimatedBottomBar.Tab(
                title = item.title.toString(),
                icon = item.icon,
                id = item.itemId,
                enabled = item.isEnabled
            )
        }
    }
}