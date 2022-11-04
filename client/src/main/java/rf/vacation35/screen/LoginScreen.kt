package rf.vacation35.screen

import android.app.ProgressDialog
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
import rf.vacation35.databinding.ActivityLoginBinding
import rf.vacation35.databinding.FragmentLoginBinding
import rf.vacation35.local.Preferences
import rf.vacation35.remote.DbApi
import splitties.fragments.start
import splitties.snackbar.snack
import timber.log.Timber
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

    @Inject
    lateinit var progressDialog: ProgressDialog

    private lateinit var binding: FragmentLoginBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.iToolbar.toolbar.title = "Авторизация"
        binding.btnLogin.setOnClickListener {
            val login = binding.etLogin.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            progressDialog.show()
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val user = withContext(Dispatchers.IO) {
                        dbApi.findUser(login, password)
                    }
                    if (user != null) {
                        preferences.userData = user.getData()
                        start<MainActivity> {
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        }
                    } else {
                        getView()?.snack("Пользователь не найден")
                    }
                } catch (e: Throwable) {
                    Timber.e(e)
                    getView()?.snack(e.message.toString())
                } finally {
                    progressDialog.dismiss()
                }
            }
        }
    }

    override fun onDestroyView() {
        progressDialog.dismiss()
        super.onDestroyView()
    }
}
