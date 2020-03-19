package nl.joery.demo.animatedbottombar.playground

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_playground.*
import nl.joery.animatedbottombar.AnimatedBottomBar
import nl.joery.demo.animatedbottombar.R
import nl.joery.demo.animatedbottombar.playground.properties.*


class PlaygroundActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playground)

        val properties = ArrayList<Property>()
        properties.add(
            CategoryProperty(
                "General"
            )
        )
        properties.add(
            ColorProperty(
                "backgroundColor"
            )
        )
        properties.add(
            CategoryProperty(
                "Tab appearance"
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
                "Animations"
            )
        )
        properties.add(
            IntegerProperty(
                "animationDuration"
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
            CategoryProperty(
                "Indicator appearance"
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
                true
            )
        )
        properties.add(
            IntegerProperty(
                "indicatorMargin",
                true
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
            EnumProperty(
                "indicatorAnimation",
                AnimatedBottomBar.IndicatorAnimation::class.java
            )
        )

        recycler.layoutManager =
            LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
        recycler.adapter = PropertyAdapter(bottom_bar, properties)
    }
}