package rf.vacation35.screen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import rf.vacation35.databinding.DialogProgressBinding

class ProgressDialog : Fragment() {

    private lateinit var binding: DialogProgressBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogProgressBinding.inflate(inflater, container, false)
        return binding.root
    }
}

