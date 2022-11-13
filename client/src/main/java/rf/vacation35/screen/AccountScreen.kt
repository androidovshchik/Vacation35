package rf.vacation35.screen

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rf.vacation35.databinding.FragmentAccountBinding
import rf.vacation35.databinding.FragmentListBinding
import rf.vacation35.databinding.ItemAccountBinding
import rf.vacation35.extension.*
import rf.vacation35.remote.DbApi
import rf.vacation35.remote.dao.UserDao
import splitties.fragments.start
import splitties.snackbar.snack
import javax.inject.Inject

@AndroidEntryPoint
class AccountListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.addFragment(android.R.id.content, AccountListFragment(), false)
    }
}

@AndroidEntryPoint
class AccountListFragment : Fragment() {

    @Inject
    lateinit var api: DbApi

    @Inject
    lateinit var progress: ProgressDialog

    private lateinit var binding: FragmentListBinding

    private var listJob: Job? = null

    @SuppressLint("SetTextI18n")
    private val adapter = abstractAdapter<ItemAccountBinding, UserDao> {
        onCreateViewHolder { parent ->
            with(ItemAccountBinding.inflate(layoutInflater, parent, false)) {
                AbstractAdapter.ViewHolder(this).apply {
                    root.setOnClickListener {
                        try {
                            val item = items[bindingAdapterPosition]
                            start<AccountActivity> {
                                putExtra("id", item.id.value)
                            }
                        } catch (ignored: Throwable) {
                        }
                    }
                }
            }
        }
        onBindViewHolder { item ->
            name.text = item.name
            login.text = "@${item.login}"
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(binding.toolbar) {
            onBackPressed {
                activity?.finish()
            }
            title = "Пользователи"
        }
        binding.rvList.adapter = adapter
        binding.fabAdd.setOnClickListener {
            start<AccountActivity>()
        }
    }

    override fun onStart() {
        super.onStart()
        listJob?.cancel()
        listJob = viewLifecycleOwner.lifecycleScope.launch {
            progress.with({
                val items = withContext(Dispatchers.IO) {
                    api.list(UserDao)
                }
                adapter.items.clear()
                adapter.items.addAll(items)
                adapter.notifyDataSetChanged()
            }, {
                view?.snack(it)
            })
        }
    }

    override fun onDestroyView() {
        progress.dismiss()
        super.onDestroyView()
    }
}

@AndroidEntryPoint
class AccountActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.addFragment(android.R.id.content, AccountFragment(), false)
    }
}

@AndroidEntryPoint
class AccountFragment : Fragment() {

    @Inject
    lateinit var api: DbApi

    @Inject
    lateinit var progress: ProgressDialog

    private lateinit var binding: FragmentAccountBinding

    private var user: UserDao? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val id = activity?.intent?.getIntExtra("id", 0) ?: 0
        with(binding.toolbar) {
            onBackPressed {
                activity?.finish()
            }
            title = if (id == 0) "Новый пользователь" else "Пользователь"
        }
        binding.btnDelete.setOnClickListener {
            context?.areYouSure {
                viewLifecycleOwner.lifecycleScope.launch {
                    progress.with({
                        withContext(Dispatchers.IO) {
                            api.delete(user!!)
                        }
                        activity?.finish()
                    }, {
                        getView()?.snack(it)
                    })
                }
            }
        }
        binding.btnSave.setOnClickListener {
            try {
                val name = binding.etName.text.toString().trim().ifEmpty { throw Throwable("Не задано имя") }
                val login = binding.etLogin.text.toString().trim().ifEmpty { throw Throwable("Не задан логин") }
                val password = binding.etPassword.text.toString().trim().ifEmpty { throw Throwable("Не задан пароль") }
                val accessBooking = binding.cbBookings.isChecked
                val accessPrice = binding.cbPrices.isChecked
                val admin = binding.cbAdmin.isChecked
                viewLifecycleOwner.lifecycleScope.launch {
                    progress.with({
                        withContext(Dispatchers.IO) {
                            if (user == null) {
                                user = api.create(UserDao) {
                                    it.name = name
                                    it.login = login
                                    it.password = password
                                    it.accessBooking = accessBooking
                                    it.accessPrice = accessPrice
                                    it.admin = admin
                                }
                            } else {
                                api.update(user!!) {
                                    it.name = name
                                    it.login = login
                                    it.password = password
                                    it.accessBooking = accessBooking
                                    it.accessPrice = accessPrice
                                    it.admin = admin
                                }
                            }
                        }
                        binding.toolbar.title = "Пользователь"
                        binding.btnDelete.isEnabled = true
                    }, {
                        getView()?.snack(it)
                    })
                }
            } catch (e: Throwable) {
                getView()?.snack(e)
            }
        }
        if (id > 0) {
            viewLifecycleOwner.lifecycleScope.launch {
                progress.with({
                    user = withContext(Dispatchers.IO) {
                        api.find(UserDao, id)
                    }
                    user?.let {
                        binding.etName.setText(it.name)
                        binding.etLogin.setText(it.login)
                        binding.etPassword.setText(it.password)
                        binding.cbBookings.isChecked = it.accessBooking
                        binding.cbPrices.isChecked = it.accessPrice
                        binding.cbAdmin.isChecked = it.admin
                        binding.btnDelete.isEnabled = true
                        binding.btnSave.isEnabled = true
                    }
                    if (user == null) {
                        getView()?.snack("Пользователь не найден")
                    }
                }, {
                    getView()?.snack(it)
                })
            }
        } else {
            binding.btnSave.isEnabled = true
        }
    }

    override fun onDestroyView() {
        progress.dismiss()
        super.onDestroyView()
    }
}
