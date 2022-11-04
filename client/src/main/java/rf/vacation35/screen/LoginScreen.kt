package rf.vacation35.screen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import rf.vacation35.databinding.ActivityLoginBinding
import rf.vacation35.databinding.FragmentLoginBinding
import rf.vacation35.local.Preferences
import rf.vacation35.remote.DbApi
import splitties.snackbar.snack
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}

@AndroidEntryPoint
class LoginFragment : Fragment() {

    @Inject
    lateinit var dbApi: DbApi

    @Inject
    lateinit var preferences: Preferences

    private lateinit var binding: FragmentLoginBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnLogin.setOnClickListener {
            val login = binding.etLogin.text.toString()
            val password = binding.etPassword.text.toString()
            val user = dbApi.findUser(login, password)
            if (user != null) {
                getView()?.snack("Найден")
            } else {
                getView()?.snack("Пользователь не найден")
            }
        }
    }
}
