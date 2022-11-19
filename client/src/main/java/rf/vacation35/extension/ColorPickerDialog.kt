package rf.vacation35.extension

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.github.dhaval2404.colorpicker.ColorPickerDialog
import com.github.dhaval2404.colorpicker.ColorPickerView
import com.github.dhaval2404.colorpicker.adapter.RecentColorAdapter
import com.github.dhaval2404.colorpicker.listener.ColorListener
import com.github.dhaval2404.colorpicker.util.ColorUtil
import com.github.dhaval2404.colorpicker.util.SharedPref
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.card.MaterialCardView
import org.json.JSONArray
import rf.vacation35.R

fun ColorPickerDialog.Builder.customShow() {
    with(build()) {
        // Create Dialog Instance
        val dialog = AlertDialog.Builder(context)
            .setTitle(title)
            .setNegativeButton(negativeButton, null)

        // Setup Custom View
        val inflater: LayoutInflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.dialog_color_picker, null) as View
        dialog.setView(dialogView)

        val colorPicker = dialogView.findViewById<ColorPickerView>(R.id.colorPicker)
        val colorView = dialogView.findViewById<MaterialCardView>(R.id.colorView)
        val recentColorsRV = dialogView.findViewById<RecyclerView>(R.id.recentColorsRV)

        val initialColor = if (!defaultColor.isNullOrBlank()) {
            Color.parseColor(defaultColor)
        } else {
            ContextCompat.getColor(context, R.color.grey_500)
        }
        colorView.setCardBackgroundColor(initialColor)

        colorPicker.setColor(initialColor)
        colorPicker.setColorListener { color, _ ->
            colorView.setCardBackgroundColor(color)
        }

        val sharedPref = SharedPref(context)
        val colorList = sharedPref.getRecentColors()

        // Setup Color Listing Adapter
        val adapter = RecentColorAdapter(colorList)
        adapter.setColorShape(colorShape)
        adapter.setColorListener(object : ColorListener {
            override fun onColorSelected(color: Int, colorHex: String) {
                colorPicker.setColor(color)
                colorView.setCardBackgroundColor(color)
            }
        })

        recentColorsRV.layoutManager = FlexboxLayoutManager(context)
        recentColorsRV.adapter = adapter

        // Set Submit Click Listener
        dialog.setPositiveButton(positiveButton) { _, _ ->
            val color = colorPicker.getColor()
            val colorHex = ColorUtil.formatColor(color)
            colorListener?.onColorSelected(color, colorHex)
            sharedPref.addColor(color = colorHex)
        }

        dismissListener?.let { listener ->
            dialog.setOnDismissListener {
                listener.onDismiss()
            }
        }

        // Create AlertDialog
        val alertDialog = dialog.create()

        // Show Dialog
        alertDialog.show()
    }
}

fun Context.updateRecentColors() {
    val preferences = getSharedPreferences("com.github.dhaval2404.colorpicker", Context.MODE_PRIVATE)
    preferences.edit()
        .putString("recent_colors", JSONArray(resources.getStringArray(R.array.colors)).toString())
        .apply()
}
