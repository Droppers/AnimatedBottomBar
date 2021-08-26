package nl.joery.animatedbottombar

import android.os.Parcel
import android.os.Parcelable
import android.view.View

internal class SavedState: View.BaseSavedState {
    var selectedIndex: Int = 0

    constructor(source: Parcel): super(source) {
        selectedIndex = source.readInt()
    }

    constructor(superState: Parcelable?): super(superState)

    override fun writeToParcel(out: Parcel, flags: Int) {
        super.writeToParcel(out, flags)
        out.writeInt(selectedIndex)
    }

    companion object {
        @JvmField
        val CREATOR = object: Parcelable.Creator<SavedState> {
            override fun createFromParcel(source: Parcel) = SavedState(source)
            override fun newArray(size: Int) = arrayOfNulls<SavedState>(size)
        }
    }
}