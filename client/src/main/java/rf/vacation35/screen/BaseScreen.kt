package rf.vacation35.screen

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rf.vacation35.EXTRA_BASE_TITLE
import rf.vacation35.EXTRA_ID
import rf.vacation35.R
import rf.vacation35.databinding.FragmentBaseBinding
import rf.vacation35.databinding.FragmentListBinding
import rf.vacation35.databinding.ItemBaseBinding
import rf.vacation35.extension.*
import rf.vacation35.local.Preferences
import rf.vacation35.remote.DbApi
import rf.vacation35.remote.dao.Base
import splitties.fragments.start
import splitties.snackbar.snack
import javax.inject.Inject

@AndroidEntryPoint
class BaseListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager.addFragment(android.R.id.content, BaseListFragment(), false)
        }
    }
}

@AndroidEntryPoint
class BaseListFragment : Fragment() {

    @Inject
    lateinit var api: DbApi

    @Inject
    lateinit var preferences: Preferences

    private val progress = ProgressDialog()

    private lateinit var binding: FragmentListBinding

    private var listJob: Job? = null

    @SuppressLint("SetTextI18n")
    private val adapter = abstractAdapter<ItemBaseBinding, Base> {
        onCreateViewHolder { parent ->
            with(ItemBaseBinding.inflate(layoutInflater, parent, false)) {
                AbstractAdapter.ViewHolder(this).apply {
                    root.setOnClickListener {
                        try {
                            val item = items[bindingAdapterPosition]
                            start<BaseActivity> {
                                putExtra(EXTRA_ID, item.id.value)
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
        childFragmentManager.hideFragment(R.id.f_filter)
    }

    override fun onStart() {
        super.onStart()
        val user = preferences.user!!
        binding.fabAdd.isVisible = user.admin
        listJob?.cancel()
        listJob = viewLifecycleOwner.lifecycleScope.launch {
            childFragmentManager.with(R.id.fl_fullscreen, progress, {
                val items = withContext(Dispatchers.IO) {
                    api.list(Base)
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
        childFragmentManager.removeFragment(progress)
        super.onDestroyView()
    }
}

@AndroidEntryPoint
class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager.addFragment(android.R.id.content, BaseFragment(), false)
        }
    }
}

@AndroidEntryPoint
class BaseFragment : Fragment() {

    @Inject
    lateinit var api: DbApi

    @Inject
    lateinit var preferences: Preferences

    private val progress = ProgressDialog()

    private lateinit var binding: FragmentBaseBinding

    private var base: Base? = null

    private var findJob: Job? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentBaseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val id = activity?.intent?.getIntExtra(EXTRA_ID, 0) ?: 0
        with(binding.toolbar) {
            onBackPressed {
                activity?.finish()
            }
            title = if (id == 0) "Новая база отдыха" else "База отдыха"
            inflateNavMenu()
        }
        binding.btnBuildings.setOnClickListener {
            start<BuildingListActivity> {
                putExtra(EXTRA_ID, base!!.id.value)
                putExtra(EXTRA_BASE_TITLE, base!!.name)
            }
        }
        binding.btnDelete.setOnClickListener {
            context?.areYouSure {
                viewLifecycleOwner.lifecycleScope.launch {
                    childFragmentManager.with(R.id.fl_fullscreen, progress, {
                        withContext(Dispatchers.IO) {
                            api.delete(base!!)
                        }
                        activity?.finish()
                    }, {
                        getView()?.snack(it)
                    })
                }
            }
        }
        val user = preferences.user!!
        binding.btnSave.isEnabled = id <= 0 && user.admin
        binding.btnSave.setOnClickListener {
            try {
                val name = binding.etName.text.toString().trim().ifEmpty { throw Throwable("Не задано имя") }
                viewLifecycleOwner.lifecycleScope.launch {
                    childFragmentManager.with(R.id.fl_fullscreen, progress, {
                        withContext(Dispatchers.IO) {
                            if (base == null) {
                                base = api.create(Base) {
                                    it.name = name
                                }
                            } else {
                                api.update(base!!) {
                                    it.name = name
                                }
                            }
                        }
                        @Suppress("NAME_SHADOWING")
                        val user = preferences.user!!
                        binding.toolbar.title = "База отдыха"
                        binding.btnBuildings.isEnabled = true
                        binding.btnDelete.isEnabled = user.admin
                    }, {
                        getView()?.snack(it)
                    })
                }
            } catch (e: Throwable) {
                getView()?.snack(e)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val id = activity?.intent?.getIntExtra(EXTRA_ID, 0) ?: 0
        if (id > 0 || base != null) {
            findJob?.cancel()
            findJob = viewLifecycleOwner.lifecycleScope.launch {
                childFragmentManager.with(R.id.fl_fullscreen, progress, {
                    base = withContext(Dispatchers.IO) {
                        api.find(Base, id)
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
                }, {
                    view?.snack(it)
                })
            }
        }
    }

    override fun onDestroyView() {
        childFragmentManager.removeFragment(progress)
        super.onDestroyView()
    }
}
