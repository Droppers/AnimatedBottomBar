package nl.joery.demo.animatedbottombar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_sample.*


class SampleFragment : Fragment() {
    companion object {
        fun newInstance(content: String): SampleFragment {
            val instance = SampleFragment()
            val args = Bundle()
            args.putString("content", content)
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
        val content = arguments?.getString("content") ?: "";
        text_content.text = content
    }
}