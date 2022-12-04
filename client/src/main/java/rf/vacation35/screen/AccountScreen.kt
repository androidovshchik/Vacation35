
package rf.vacation35.screen

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rf.vacation35.EXTRA_USER_ID
import rf.vacation35.R
import rf.vacation35.databinding.FragmentAccountBinding
import rf.vacation35.databinding.FragmentListBinding
import rf.vacation35.databinding.ItemAccountBinding
import rf.vacation35.extension.*
import rf.vacation35.remote.dao.User
import splitties.fragments.start
import splitties.snackbar.snack

@AndroidEntryPoint
class AccountListActivity : AbstractActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager.addFragment(android.R.id.content, AccountListFragment(), false)
        }
    }
}

@AndroidEntryPoint
class AccountListFragment : AbstractFragment() {

    @SuppressLint("SetTextI18n")
    private val adapter = abstractAdapter<ItemAccountBinding, User> {
        onCreateViewHolder { parent ->
            with(ItemAccountBinding.inflate(layoutInflater, parent, false)) {
                AbstractAdapter.ViewHolder(this).apply {
                    root.setOnClickListener {
                        try {
                            val item = items[bindingAdapterPosition]
                            start<AccountActivity> {
                                putExtra(EXTRA_USER_ID, item.id.value)
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

    private lateinit var binding: FragmentListBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.toolbar) {
            onBackPressed {
                activity?.finish()
            }
            title = "Пользователи"
            inflateNavMenu<AccountListFragment>()
        }
        binding.rvList.adapter = adapter
        binding.fabAdd.setOnClickListener {
            start<AccountActivity>()
        }
        childFragmentManager.hideFragment(R.id.f_bb)
    }

    override fun readOnStart() {
        super.readOnStart()
        startJob?.cancel()
        startJob = viewLifecycleOwner.lifecycleScope.launch {
            useProgress(::readOnStart) {
                val items = withContext(Dispatchers.IO) {
                    api.list(User)
                }
                adapter.items.clear()
                adapter.items.addAll(items)
                adapter.notifyDataSetChanged()
            }
        }
    }
}

@AndroidEntryPoint
class AccountActivity : AbstractActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager.addFragment(android.R.id.content, AccountFragment(), false)
        }
    }
}

@AndroidEntryPoint
class AccountFragment : AbstractFragment() {

    private var user: User? = null

    private lateinit var binding: FragmentAccountBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.toolbar) {
            onBackPressed {
                activity?.finish()
            }
            title = if (argUserId == 0) "Новый пользователь" else "Пользователь"
            inflateNavMenu<AccountFragment>()
        }
        binding.btnDelete.setOnClickListener {
            context?.areYouSure {
                delete()
            }
        }
        binding.btnSave.isEnabled = argUserId <= 0
        binding.btnSave.setOnClickListener {
            upsert()
        }
    }

    override fun readOnStart() {
        super.readOnStart()
        if (user == null && argUserId <= 0) {
            return
        }
        startJob?.cancel()
        startJob = viewLifecycleOwner.lifecycleScope.launch {
            useProgress(::readOnStart) {
                user = withContext(Dispatchers.IO) {
                    api.find(User, user?.id?.value ?: argUserId)
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
                    view?.snack("Пользователь не найден")
                }
            }
        }
    }

    override fun upsert() {
        try {
            val name = binding.etName.value.ifEmpty { throw Throwable("Не задано имя") }
            val login = binding.etLogin.value.ifEmpty { throw Throwable("Не задан логин") }
            val password = binding.etPassword.value.ifEmpty { throw Throwable("Не задан пароль") }
            val accessBooking = binding.cbBookings.isChecked
            val accessPrice = binding.cbPrices.isChecked
            val admin = binding.cbAdmin.isChecked
            viewLifecycleOwner.lifecycleScope.launch {
                useProgress(::upsert) {
                    withContext(Dispatchers.IO) {
                        if (user == null) {
                            user = api.insert(User) {
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
                }
            }
        } catch (e: Throwable) {
            view?.snack(e)
        }
    }

    override fun delete() {
        viewLifecycleOwner.lifecycleScope.launch {
            useProgress(::delete) {
                withContext(Dispatchers.IO) {
                    api.delete(user!!)
                }
                activity?.finish()
            }
        }
    }
}
