@file:Suppress("UNCHECKED_CAST")

package nl.joery.demo.animatedbottombar.playground

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener
import nl.joery.animatedbottombar.AnimatedBottomBar
import nl.joery.demo.animatedbottombar.R
import nl.joery.demo.animatedbottombar.Utils
import nl.joery.demo.animatedbottombar.dp
import nl.joery.demo.animatedbottombar.playground.properties.*
import nl.joery.demo.animatedbottombar.px


internal class PropertyAdapter(
    private val bottomBar: AnimatedBottomBar,
    private val properties: List<Property>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemCount(): Int {
        return properties.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(getLayout(viewType), parent, false) as View
        return when (viewType) {
            Property.TYPE_ENUM -> EnumHolder(v, bottomBar)
            Property.TYPE_COLOR -> ColorHolder(v, bottomBar)
            Property.TYPE_BOOLEAN -> BooleanHolder(v, bottomBar)
            Property.TYPE_CATEGORY -> CategoryHolder(v)
            else -> IntegerHolder(v, bottomBar)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is BaseHolder<*>) {
            (holder as BaseHolder<Property>).bind(properties[position])
        } else {
            (holder as CategoryHolder).bind(properties[position] as CategoryProperty)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (properties[position]) {
            is EnumProperty -> Property.TYPE_ENUM
            is ColorProperty -> Property.TYPE_COLOR
            is IntegerProperty -> Property.TYPE_INTEGER
            is BooleanProperty -> Property.TYPE_BOOLEAN
            is CategoryProperty -> Property.TYPE_CATEGORY
            else -> -1
        }
    }

    @LayoutRes
    private fun getLayout(propertyType: Int): Int {
        return when (propertyType) {
            Property.TYPE_CATEGORY -> R.layout.list_property_category
            Property.TYPE_COLOR -> R.layout.list_property_color
            Property.TYPE_BOOLEAN -> R.layout.list_property_boolean
            else -> R.layout.list_property
        }
    }

    class CategoryHolder(
        view: View
    ) :
        RecyclerView.ViewHolder(view) {
        internal val name = view.findViewById<TextView>(R.id.name)

        fun bind(category: CategoryProperty) {
            name.text = category.name
        }
    }

    abstract class BaseHolder<T : Property>(
        internal val view: View,
        internal val bottomBar: AnimatedBottomBar
    ) :
        RecyclerView.ViewHolder(view) {
        internal lateinit var property: T

        internal val name = view.findViewById<TextView>(R.id.name)
        private val value = view.findViewById<TextView>(R.id.value)

        init {
            view.setOnClickListener {
                handleClick()
            }
        }

        @SuppressLint("DefaultLocale")
        protected open fun getValue(): String {
            return Utils.getProperty(bottomBar, property.name).toString().toLowerCase().capitalize()
        }

        protected abstract fun handleClick()

        protected open fun updateValue() {
            if (value == null) {
                return
            }

            value.text = getValue()
        }

        internal open fun bind(property: T) {
            this.property = property
            name.text = property.name

            updateValue()
        }
    }

    class EnumHolder(v: View, bottomBar: AnimatedBottomBar) :
        BaseHolder<EnumProperty>(v, bottomBar) {

        @SuppressLint("DefaultLocale")
        override fun handleClick() {
            val enumValues = property.enumClass.enumConstants as Array<Enum<*>>
            val items = enumValues.map { it.name.toLowerCase().capitalize() }.toTypedArray()

            val builder: AlertDialog.Builder = AlertDialog.Builder(view.context)
            builder.setTitle("Set ${property.name} value")
            builder.setSingleChoiceItems(
                items, items.indexOf(getValue())
            ) { dialog, item ->
                Utils.setProperty(bottomBar, property.name, enumValues.first {
                    it.name == items[item].toUpperCase()
                })
                updateValue()
                dialog.dismiss()
            }
            val dialog = builder.create()
            dialog.show()
        }
    }

    class ColorHolder(v: View, bottomBar: AnimatedBottomBar) :
        BaseHolder<ColorProperty>(v, bottomBar) {

        private val color = view.findViewById<MaterialCardView>(R.id.color)

        override fun getValue(): String {
            return "#%06X".format(0xFFFFFF and getColor())
        }

        override fun handleClick() {
            val activity = view.context as FragmentActivity
            val builder = ColorPickerDialog.newBuilder()
                .setAllowCustom(true)
                .setAllowPresets(true)
                .setShowColorShades(true)

            val dialog = builder.create()
            dialog.setColorPickerDialogListener(object : ColorPickerDialogListener {
                override fun onDialogDismissed(dialogId: Int) {
                }

                override fun onColorSelected(dialogId: Int, color: Int) {
                    Utils.setProperty(bottomBar, property.name, color)
                    updateValue()
                    updateColor()
                }
            })
            dialog.show(activity.supportFragmentManager, "")
        }

        private fun updateColor() {
            color.setCardBackgroundColor(getColor())
        }

        private fun getColor(): Int {
            return Utils.getProperty(bottomBar, property.name) as Int? ?: 0
        }

        override fun bind(property: ColorProperty) {
            super.bind(property)

            updateColor()
        }
    }

    class IntegerHolder(v: View, bottomBar: AnimatedBottomBar) :
        BaseHolder<IntegerProperty>(v, bottomBar) {

        override fun getValue(): String {
            val value = super.getValue()
            return if (property.dimension)
                value.toInt().dp.toString() + "dp"
            else
                value
        }

        @SuppressLint("InflateParams")
        override fun handleClick() {
            val view = LayoutInflater.from(view.context).inflate(
                R.layout.view_text_input,
                null
            )
            val editText = view.findViewById<TextInputEditText>(R.id.edit_text)
            editText.setText(getValue().replace("dp", ""))

            val builder: AlertDialog.Builder = AlertDialog.Builder(view.context)
                .setTitle("Set ${property.name} value")
                .setPositiveButton("Select") { dialog, _ ->
                    var newValue = editText.text.toString().toInt()
                    if (property.dimension) {
                        newValue = newValue.px
                    }
                    Utils.setProperty(bottomBar, property.name, newValue)
                    updateValue()
                    dialog.dismiss()
                }
                .setView(view)

            val dialog = builder.create()
            dialog.show()
        }
    }

    class BooleanHolder(v: View, bottomBar: AnimatedBottomBar) :
        BaseHolder<BooleanProperty>(v, bottomBar) {
        private val booleanSwitch = view.findViewById<SwitchMaterial>(R.id.booleanSwitch)

        override fun updateValue() {
            booleanSwitch.isChecked = Utils.getProperty(bottomBar, property.name) as Boolean
        }

        override fun handleClick() {
        }

        override fun bind(property: BooleanProperty) {
            super.bind(property)

            booleanSwitch.setOnCheckedChangeListener { _, isChecked ->
                Utils.setProperty(bottomBar, property.name, isChecked)
                updateValue()
            }
        }
    }
}