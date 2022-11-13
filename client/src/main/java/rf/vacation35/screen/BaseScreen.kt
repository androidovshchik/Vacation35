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
import rf.vacation35.databinding.FragmentBaseBinding
import rf.vacation35.databinding.FragmentListBinding
import rf.vacation35.databinding.ItemBaseBinding
import rf.vacation35.extension.*
import rf.vacation35.remote.DbApi
import rf.vacation35.remote.dao.BaseDao
import splitties.fragments.start
import splitties.snackbar.snack
import javax.inject.Inject

@AndroidEntryPoint
class BaseListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.addFragment(android.R.id.content, BaseListFragment(), false)
    }
}

@AndroidEntryPoint
class BaseListFragment : Fragment() {

    @Inject
    lateinit var api: DbApi

    @Inject
    lateinit var progress: ProgressDialog

    private lateinit var binding: FragmentListBinding

    private var listJob: Job? = null

    @SuppressLint("SetTextI18n")
    private val adapter = abstractAdapter<ItemBaseBinding, BaseDao> {
        onCreateViewHolder { parent ->
            with(ItemBaseBinding.inflate(layoutInflater, parent, false)) {
                AbstractAdapter.ViewHolder(this).apply {
                    root.setOnClickListener {
                        try {
                            val item = items[bindingAdapterPosition]
                            start<BaseActivity> {
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
        }
        binding.rvList.adapter = adapter
        binding.fabAdd.setOnClickListener {
            start<BaseActivity>()
        }
    }

    override fun onStart() {
        super.onStart()
        listJob?.cancel()
        listJob = viewLifecycleOwner.lifecycleScope.launch {
            progress.with({
                val items = withContext(Dispatchers.IO) {
                    api.list(BaseDao)
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
class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.addFragment(android.R.id.content, BaseFragment(), false)
    }
}

@AndroidEntryPoint
class BaseFragment : Fragment() {

    @Inject
    lateinit var api: DbApi

    @Inject
    lateinit var progress: ProgressDialog

    private lateinit var binding: FragmentBaseBinding

    private var base: BaseDao? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentBaseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val id = activity?.intent?.getIntExtra("id", 0) ?: 0
        with(binding.toolbar) {
            onBackPressed {
                activity?.finish()
            }
            title = if (id == 0) "Новая база отдыха" else "База отдыха"
        }
        binding.btnDelete.setOnClickListener {
            context?.areYouSure {
                viewLifecycleOwner.lifecycleScope.launch {
                    progress.with({
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
        binding.btnSave.setOnClickListener {
            try {
                val name = binding.etName.text.toString().trim().ifEmpty { throw Throwable("Не задано имя") }
                viewLifecycleOwner.lifecycleScope.launch {
                    progress.with({
                        withContext(Dispatchers.IO) {
                            if (base == null) {
                                base = api.create(BaseDao) {
                                    it.name = name
                                }
                            } else {
                                api.update(base!!) {
                                    it.name = name
                                }
                            }
                        }
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
                    base = withContext(Dispatchers.IO) {
                        api.find(BaseDao, id)
                    }
                    base?.let {
                        binding.etName.setText(it.name)
                        binding.btnDelete.isEnabled = true
                        binding.btnSave.isEnabled = true
                    }
                    if (base == null) {
                        getView()?.snack("База отдыха не найдена")
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
