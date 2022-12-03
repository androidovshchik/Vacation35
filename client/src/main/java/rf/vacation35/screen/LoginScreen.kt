package rf.vacation35.screen

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rf.vacation35.databinding.FragmentLoginBinding
import rf.vacation35.extension.addFragment
import rf.vacation35.extension.value
import rf.vacation35.local.Preferences
import splitties.activities.start
import splitties.fragments.start
import splitties.snackbar.snack
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AbstractActivity() {

    @Inject
    lateinit var preferences: Preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (preferences.user != null) {
            start<MainActivity>()
            finish()
            return
        }
        if (savedInstanceState == null) {
            supportFragmentManager.addFragment(android.R.id.content, LoginFragment())
        }
    }
}

@AndroidEntryPoint
class LoginFragment : AbstractFragment() {

    @Inject
    lateinit var preferences: Preferences

    private lateinit var binding: FragmentLoginBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.toolbar) {
            title = "Авторизация"
        }
        binding.btnLogin.setOnClickListener {
            login()
        }
    }

    private fun login() {
        val login = binding.etLogin.value
        val password = binding.etPassword.value
        viewLifecycleOwner.lifecycleScope.launch {
            useProgress(::login) {
                val user = withContext(Dispatchers.IO) {
                    api.findUser(login, password)
                }
                if (user != null) {
                    preferences.user = user.toRaw()
                    start<MainActivity> {
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    }
                } else {
                    view?.snack("Неверные данные для входа")
                }
            }
        }
    }
}
