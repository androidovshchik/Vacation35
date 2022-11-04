package rf.vacation35.screen

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rf.vacation35.R
import rf.vacation35.databinding.ActivityAccountListBinding
import rf.vacation35.databinding.FragmentAccountListBinding
import rf.vacation35.databinding.ItemAccountBinding
import rf.vacation35.remote.DbApi
import rf.vacation35.remote.dao.UserDao
import splitties.systemservices.layoutInflater
import java.lang.ref.WeakReference
import javax.inject.Inject

@AndroidEntryPoint
class AccountListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAccountListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountListBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}

@AndroidEntryPoint
class AccountListFragment : Fragment(), AccountAdapter.Listener {

    @Inject
    lateinit var dbApi: DbApi

    private val adapter = AccountAdapter(this)

    private lateinit var binding: FragmentAccountListBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAccountListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(binding.iToolbar.toolbar) {
            title = "Пользователи"
            setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
            setNavigationOnClickListener {

            }
        }
        binding.rvList.adapter = adapter
        binding.fabAdd.setOnClickListener {

        }
        viewLifecycleOwner.lifecycleScope.launch {
            val users = withContext(Dispatchers.IO) {
                dbApi.listUsers()
            }
            adapter.items.clear()
            adapter.items.addAll(users)
            adapter.notifyDataSetChanged()
        }
    }

    override fun onUserClicked(position: Int, user: UserDao) {

    }
}

class AccountAdapter(listener: Listener) : RecyclerView.Adapter<AccountAdapter.ViewHolder>() {

    private val reference = WeakReference(listener)

    val items = mutableListOf<UserDao>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAccountBinding.inflate(parent.context.layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = items[position]
        holder.bindView(user)
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(private val binding: ItemAccountBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                try {
                    val position = bindingAdapterPosition
                    val user = items[position]
                    reference.get()?.onUserClicked(position, user)
                } catch (ignored: Throwable) {
                }
            }
        }

        @SuppressLint("SetTextI18n")
        fun bindView(user: UserDao) {
            with(binding.root.context) {
                binding.name.text = user.name
                binding.login.text = "@${user.login}"
            }
        }
    }

    interface Listener {

        fun onUserClicked(position: Int, user: UserDao)
    }
}
