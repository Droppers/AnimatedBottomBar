@file:Suppress("UNCHECKED_CAST")

package nl.joery.demo.animatedbottombar.playground

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener
import nl.joery.animatedbottombar.AnimatedBottomBar
import nl.joery.demo.animatedbottombar.*
import nl.joery.demo.animatedbottombar.playground.properties.*


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
            Property.TYPE_INTERPOLATOR -> InterpolatorHolder(v, bottomBar)
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
            is InterpolatorProperty -> Property.TYPE_INTERPOLATOR
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
            return ReflectionUtils.getPropertyValue(bottomBar, property.name).toString()
                .toLowerCase()
                .capitalize()
        }

        protected abstract fun handleClick()

        protected open fun updateValue() {
            if (value == null) {
                return
            }

            value.text = getValue()
        }

        protected fun setValue(value: Any) {
            ReflectionUtils.setPropertyValue(bottomBar, property.name, value)
            property.modified = true
            updateValue()
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

            MaterialAlertDialogBuilder(view.context)
                .setTitle(view.context.getString(R.string.set_property_value, property.name))
                .setSingleChoiceItems(
                    items, items.indexOf(getValue())
                ) { dialog, item ->
                    setValue(enumValues.first {
                        it.name == items[item].toUpperCase()
                    })
                    dialog.dismiss()
                }
                .show()
        }
    }

    class ColorHolder(v: View, bottomBar: AnimatedBottomBar) :
        BaseHolder<ColorProperty>(v, bottomBar) {

        private val color = view.findViewById<View>(R.id.color)

        override fun getValue(): String {
            return "#%06X".format(0xFFFFFF and getColor())
        }

        override fun handleClick() {
            val activity = view.context as FragmentActivity
            val builder = ColorPickerDialog.newBuilder()
                .setColor(ColorUtils.setAlphaComponent(getColor(), 255))
                .setAllowCustom(true)
                .setAllowPresets(true)
                .setShowColorShades(true)
                .setDialogTitle(R.string.pick_color)
                .setSelectedButtonText(R.string.apply)

            val dialog = builder.create()
            dialog.setColorPickerDialogListener(object : ColorPickerDialogListener {
                override fun onDialogDismissed(dialogId: Int) {
                }

                override fun onColorSelected(dialogId: Int, color: Int) {
                    setValue(color)
                    updateColor()
                }
            })
            dialog.show(activity.supportFragmentManager, "")
        }

        private fun updateColor() {
            val shape = GradientDrawable()
            shape.shape = GradientDrawable.RECTANGLE
            shape.cornerRadii = FloatArray(8) { 3.dpPx.toFloat() }
            shape.setColor(getColor())
            shape.setStroke(1.dpPx, Color.rgb(200, 200, 200))
            color.background = shape
        }

        private fun getColor(): Int {
            return ReflectionUtils.getPropertyValue(bottomBar, property.name) as Int? ?: 0
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
            return when (property.density) {
                TypedValue.COMPLEX_UNIT_DIP -> value.toInt().dp.toString() + "dp"
                TypedValue.COMPLEX_UNIT_SP -> value.toInt().sp.toString() + "sp"
                else -> value
            }
        }

        @SuppressLint("InflateParams")
        override fun handleClick() {
            val view = LayoutInflater.from(view.context).inflate(
                R.layout.view_text_input,
                null
            )
            val editText = view.findViewById<TextInputEditText>(R.id.edit_text)
            editText.setText(getValue().replace("[^\\dxX]+".toRegex(), ""))

            MaterialAlertDialogBuilder(view.context)
                .setTitle(view.context.getString(R.string.set_property_value, property.name))
                .setPositiveButton(R.string.apply) { dialog, _ ->
                    try {
                        var newValue = editText.text.toString().toInt()
                        newValue = when (property.density) {
                            TypedValue.COMPLEX_UNIT_DIP -> newValue.dpPx
                            TypedValue.COMPLEX_UNIT_SP -> newValue.spPx
                            else -> newValue
                        }
                        setValue(newValue)
                        dialog.dismiss()
                    } catch (e: NumberFormatException) {
                    }
                }
                .setView(view)
                .show()
        }
    }

    class BooleanHolder(v: View, bottomBar: AnimatedBottomBar) :
        BaseHolder<BooleanProperty>(v, bottomBar) {
        private val booleanSwitch = view.findViewById<SwitchMaterial>(R.id.booleanSwitch)

        override fun updateValue() {
            booleanSwitch.isChecked =
                ReflectionUtils.getPropertyValue(bottomBar, property.name) as Boolean
        }

        override fun handleClick() {
        }

        override fun bind(property: BooleanProperty) {
            super.bind(property)

            booleanSwitch.setOnCheckedChangeListener { _, isChecked ->
                setValue(isChecked)
            }
        }
    }

    class InterpolatorHolder(v: View, bottomBar: AnimatedBottomBar) :
        BaseHolder<InterpolatorProperty>(v, bottomBar) {

        override fun getValue(): String {
            val value = ReflectionUtils.getPropertyValue(bottomBar, property.name)
            return value!!::class.java.simpleName
        }

        override fun handleClick() {
            val interpolatorNames =
                InterpolatorProperty.interpolators.map { it::class.java.simpleName }.toTypedArray()

            MaterialAlertDialogBuilder(view.context)
                .setTitle(view.context.getString(R.string.set_property_value, property.name))
                .setSingleChoiceItems(
                    interpolatorNames, interpolatorNames.indexOf(getValue())
                ) { dialog, item ->
                    setValue(InterpolatorProperty.interpolators.first {
                        it::class.java.simpleName == interpolatorNames[item]
                    })
                    dialog.dismiss()
                }
                .show()
        }
    }
}