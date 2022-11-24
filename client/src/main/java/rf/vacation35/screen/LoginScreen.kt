package rf.vacation35.screen

import android.content.Intent
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
import rf.vacation35.R
import rf.vacation35.databinding.FragmentLoginBinding
import rf.vacation35.extension.addFragment
import rf.vacation35.extension.removeFragment
import rf.vacation35.extension.snack
import rf.vacation35.extension.with
import rf.vacation35.local.Preferences
import rf.vacation35.remote.DbApi
import splitties.fragments.start
import splitties.snackbar.snack
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.addFragment(android.R.id.content, LoginFragment())
    }
}

@AndroidEntryPoint
class LoginFragment : Fragment() {

    @Inject
    lateinit var api: DbApi

    @Inject
    lateinit var preferences: Preferences

    private val progress = ProgressDialog()

    private lateinit var binding: FragmentLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences.user = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(binding.toolbar) {
            title = "Авторизация"
        }
        binding.btnLogin.setOnClickListener {
            val login = binding.etLogin.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            viewLifecycleOwner.lifecycleScope.launch {
                childFragmentManager.with(R.id.fl_fullscreen, progress, {
                    val user = withContext(Dispatchers.IO) {
                        api.findUser(login, password)
                    }
                    if (user != null) {
                        preferences.user = user.toRaw()
                        start<MainActivity> {
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        }
                    } else {
                        getView()?.snack("Неверные данные для входа")
                    }
                }, {
                    getView()?.snack(it)
                })
            }
        }
    }

    override fun onDestroyView() {
        childFragmentManager.removeFragment(progress)
        super.onDestroyView()
    }
}
