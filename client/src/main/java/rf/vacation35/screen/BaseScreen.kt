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
import rf.vacation35.EXTRA_BASE_ID
import rf.vacation35.R
import rf.vacation35.databinding.FragmentBaseBinding
import rf.vacation35.databinding.FragmentListBinding
import rf.vacation35.databinding.ItemBaseBinding
import rf.vacation35.extension.*
import rf.vacation35.remote.dao.Base
import splitties.fragments.start
import splitties.snackbar.snack

@AndroidEntryPoint
class BaseListActivity : AbstractActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager.addFragment(android.R.id.content, BaseListFragment(), false)
        }
    }
}

@AndroidEntryPoint
class BaseListFragment : AbstractFragment() {

    @SuppressLint("SetTextI18n")
    private val adapter = abstractAdapter<ItemBaseBinding, Base> {
        onCreateViewHolder { parent ->
            with(ItemBaseBinding.inflate(layoutInflater, parent, false)) {
                AbstractAdapter.ViewHolder(this).apply {
                    root.setOnClickListener {
                        try {
                            val item = items[bindingAdapterPosition]
                            start<BaseActivity> {
                                putExtra(EXTRA_BASE_ID, item.id.value)
                            }
                        } catch (ignored: Throwable) {
                        }
                    }
                }
            }
        }
        onBindViewHolder { item ->
            name.text = item.name
        }
    }

    private lateinit var binding: FragmentListBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(binding.toolbar) {
            onBackPressed {
                activity?.finish()
            }
            title = "Базы отдыха"
            inflateNavMenu()
        }
        binding.rvList.adapter = adapter
        binding.fabAdd.setOnClickListener {
            start<BaseActivity>()
        }
        childFragmentManager.hideFragment(R.id.f_bb)
    }

    override fun readOnStart() {
        startJob?.cancel()
        startJob = viewLifecycleOwner.lifecycleScope.launch {
            useProgress(::readOnStart) {
                val items = withContext(Dispatchers.IO) {
                    api.list(Base)
                }
                adapter.items.clear()
                adapter.items.addAll(items)
                adapter.notifyDataSetChanged()
            }
        }
    }
}

@AndroidEntryPoint
class BaseActivity : AbstractActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager.addFragment(android.R.id.content, BaseFragment(), false)
        }
    }
}

@AndroidEntryPoint
class BaseFragment : AbstractFragment() {

    private var base: Base? = null

    private lateinit var binding: FragmentBaseBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentBaseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val user = preferences.user!!
        with(binding.toolbar) {
            onBackPressed {
                activity?.finish()
            }
            title = if (argBaseId == 0) "Новая база отдыха" else "База отдыха"
            inflateNavMenu()
        }
        binding.btnBuildings.setOnClickListener {
            start<BuildingListActivity> {
                putExtra(EXTRA_BASE_ID, base!!.id.value)
            }
        }
        binding.btnDelete.setOnClickListener {
            context?.areYouSure {
                delete()
            }
        }
        binding.btnSave.isEnabled = argBaseId <= 0 && user.admin
        binding.btnSave.setOnClickListener {
            upsert()
        }
    }

    override fun readOnStart() {
        if (base == null && argBaseId <= 0) {
            return
        }
        startJob?.cancel()
        startJob = viewLifecycleOwner.lifecycleScope.launch {
            useProgress(::readOnStart) {
                base = withContext(Dispatchers.IO) {
                    api.find(Base, base?.id?.value ?: argBaseId)
                }
                base?.let {
                    val user = preferences.user!!
                    binding.etName.setText(it.name)
                    binding.btnBuildings.isEnabled = true
                    binding.btnDelete.isEnabled = user.admin
                    binding.btnSave.isEnabled = user.admin
                }
                if (base == null) {
                    binding.btnBuildings.isEnabled = false
                    binding.btnDelete.isEnabled = false
                    binding.btnSave.isEnabled = false
                    view?.snack("База отдыха не найдена")
                }
            }
        }
    }

    override fun upsert() {
        try {
            val name = binding.etName.value.ifEmpty { throw Throwable("Не задано имя") }
            viewLifecycleOwner.lifecycleScope.launch {
                useProgress(::upsert) {
                    withContext(Dispatchers.IO) {
                        if (base == null) {
                            base = api.insert(Base) {
                                it.name = name
                            }
                        } else {
                            api.update(base!!) {
                                it.name = name
                            }
                        }
                    }
                    val user = preferences.user!!
                    binding.toolbar.title = "База отдыха"
                    binding.btnBuildings.isEnabled = true
                    binding.btnDelete.isEnabled = user.admin
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
                    api.delete(base!!)
                }
                activity?.finish()
            }
        }
    }
}
