package nl.joery.demo.animatedbottombar.playground

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_playground.*
import nl.joery.animatedbottombar.AnimatedBottomBar
import nl.joery.animatedbottombar.BottomBarStyle
import nl.joery.demo.animatedbottombar.ExampleActivity
import nl.joery.demo.animatedbottombar.R
import nl.joery.demo.animatedbottombar.playground.properties.*
import nl.joery.demo.animatedbottombar.spPx


class PlaygroundActivity : AppCompatActivity() {
    private lateinit var properties: ArrayList<Property>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playground)

        bottom_bar.setBadgeAtTabIndex(
            1, AnimatedBottomBar.Badge(
                text = "99",
                backgroundColor = Color.RED,
                textColor = Color.GREEN,
                textSize = 12.spPx
            )
        )

        initProperties()
        initRecyclerView()

        view_xml.setOnClickListener {
            showXmlDialog()
        }

        open_examples.setOnClickListener {
            startActivity(Intent(this, ExampleActivity::class.java))
        }
    }

    private fun initProperties() {
        properties = ArrayList()
        properties.add(
            CategoryProperty(
                getString(R.string.category_general)
            )
        )
        properties.add(
            ColorProperty(
                "backgroundColor"
            )
        )
        properties.add(
            CategoryProperty(
                getString(R.string.category_tab)
            )
        )
        properties.add(
            EnumProperty(
                "selectedTabType",
                AnimatedBottomBar.TabType::class.java
            )
        )
        properties.add(
            ColorProperty(
                "tabColor"
            )
        )
        properties.add(
            ColorProperty(
                "tabColorSelected"
            )
        )
        properties.add(
            ColorProperty(
                "tabColorDisabled"
            )
        )
        properties.add(
            IntegerProperty(
                "textSize",
                TypedValue.COMPLEX_UNIT_SP
            )
        )
        properties.add(
            IntegerProperty(
                "iconSize",
                TypedValue.COMPLEX_UNIT_DIP
            )
        )
        properties.add(
            BooleanProperty(
                "rippleEnabled"
            )
        )
        properties.add(
            ColorProperty(
                "rippleColor"
            )
        )

        properties.add(
            CategoryProperty(
                getString(R.string.category_indicator)
            )
        )
        properties.add(
            ColorProperty(
                "indicatorColor"
            )
        )
        properties.add(
            IntegerProperty(
                "indicatorHeight",
                TypedValue.COMPLEX_UNIT_DIP
            )
        )
        properties.add(
            IntegerProperty(
                "indicatorMargin",
                TypedValue.COMPLEX_UNIT_DIP
            )
        )
        properties.add(
            EnumProperty(
                "indicatorAppearance",
                AnimatedBottomBar.IndicatorAppearance::class.java
            )
        )
        properties.add(
            EnumProperty(
                "indicatorLocation",
                AnimatedBottomBar.IndicatorLocation::class.java
            )
        )

        properties.add(
            CategoryProperty(
                getString(R.string.category_animations)
            )
        )
        properties.add(
            IntegerProperty(
                "animationDuration"
            )
        )
        properties.add(
            InterpolatorProperty(
                "animationInterpolator"
            )
        )
        properties.add(
            EnumProperty(
                "tabAnimation",
                AnimatedBottomBar.TabAnimation::class.java
            )
        )
        properties.add(
            EnumProperty(
                "tabAnimationSelected",
                AnimatedBottomBar.TabAnimation::class.java
            )
        )
        properties.add(
            EnumProperty(
                "indicatorAnimation",
                AnimatedBottomBar.IndicatorAnimation::class.java
            )
        )

        properties.add(
            CategoryProperty(
                getString(R.string.category_animations)
            )
        )
        properties.add(
            EnumProperty(
                "badgeAnimation",
                AnimatedBottomBar.BadgeAnimation::class.java
            )
        )
        properties.add(
            IntegerProperty(
                "badgeAnimationDuration"
            )
        )
        properties.add(
            ColorProperty(
                "badgeBackgroundColor"
            )
        )
        properties.add(
            ColorProperty(
                "badgeTextColor"
            )
        )
        properties.add(
            IntegerProperty(
                "badgeTextSize",
                TypedValue.COMPLEX_UNIT_SP
            )
        )
    }

    private fun initRecyclerView() {
        recycler.layoutManager =
            LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
        recycler.adapter = PropertyAdapter(bottom_bar, properties)
    }

    private fun showXmlDialog() {
        val html = XmlGenerator.generateHtmlXml(
            "nl.joery.animatedbottombar.AnimatedBottomBar",
            "abb",
            bottom_bar,
            properties,
            arrayOf(BottomBarStyle.Tab(), BottomBarStyle.Indicator())
        )

        val layout = LayoutInflater.from(this).inflate(R.layout.view_generated_xml, null)
        val textView = layout.findViewById<TextView>(R.id.xml)
        textView.setHorizontallyScrolling(true)
        textView.text = htmlToSpanned(html)

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.generate_xml_title)
            .setView(layout)
            .setPositiveButton(R.string.copy_to_clipboard) { _, _ ->
                val clipboard =
                    getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip =
                    ClipData.newPlainText(getString(R.string.generate_xml_title), htmlToText(html))
                clipboard.setPrimaryClip(clip)

                Snackbar.make(
                    findViewById<View>(android.R.id.content),
                    R.string.copied_xml_clipboard,
                    Snackbar.LENGTH_LONG
                ).show()
            }
            .show()
    }

    private fun htmlToText(html: String): String {
        return htmlToSpanned(html).toString().replace("\u00A0", " ")
    }

    private fun htmlToSpanned(html: String): Spanned {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(html)
        }
    }
}