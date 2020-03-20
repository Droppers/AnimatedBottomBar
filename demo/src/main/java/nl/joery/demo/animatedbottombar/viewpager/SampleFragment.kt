package nl.joery.demo.animatedbottombar.viewpager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_sample.*
import nl.joery.demo.animatedbottombar.R


class SampleFragment : Fragment() {
    companion object {
        fun newInstance(position: Int): SampleFragment {
            val instance =
                SampleFragment()
            val args = Bundle()
            args.putInt("position", position)
            instance.arguments = args
            return instance
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sample, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val position = arguments?.getInt("position", -1) ?: -1
        text_content.text = getString(R.string.sample_fragment_content, position)
    }
}