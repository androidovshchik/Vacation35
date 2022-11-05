package rf.vacation35.screen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rf.vacation35.databinding.ActivityAccountBinding
import rf.vacation35.databinding.FragmentAccountBinding
import rf.vacation35.remote.DbApi
import javax.inject.Inject

@AndroidEntryPoint
class BaseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAccountBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}

@AndroidEntryPoint
class BaseFragment : Fragment() {

    @Inject
    lateinit var dbApi: DbApi

    private lateinit var binding: FragmentAccountBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val id = activity?.intent?.getIntExtra("id", 0) ?: 0
        viewLifecycleOwner.lifecycleScope.launch {
            val base = withContext(Dispatchers.IO) {
                dbApi.findBase(id)
            }
            if (base != null) {

            } else {

            }
        }
    }
}
