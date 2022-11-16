package rf.vacation35.screen

import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

inline fun <V : ViewBinding, T> abstractAdapter(items: List<T> = mutableListOf(), body: AbstractAdapter<V, T>.() -> Unit): AbstractAdapter<V, T> {
    return AbstractAdapter<V, T>(items.toMutableList()).apply { body() }
}

open class AbstractAdapter<V : ViewBinding, T>(val items: MutableList<T>) : RecyclerView.Adapter<AbstractAdapter.ViewHolder<V>>() {

    private lateinit var _onCreateViewHolder: (parent: ViewGroup) -> ViewHolder<V>

    private var _onBindViewHolder: V.(item: T) -> Unit = { _ -> }

    fun onCreateViewHolder(body: (parent: ViewGroup) -> ViewHolder<V>) {
        _onCreateViewHolder = body
    }

    fun onBindViewHolder(body: V.(item: T) -> Unit) {
        _onBindViewHolder = body
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<V> {
        return _onCreateViewHolder(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder<V>, position: Int) {
        val item = items[position]
        _onBindViewHolder(holder.binding, item)
    }

    override fun getItemCount(): Int = items.count()

    class ViewHolder<V : ViewBinding>(val binding: V) : RecyclerView.ViewHolder(binding.root)
}

abstract class EndlessListener : RecyclerView.OnScrollListener() {

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        val manager = recyclerView.layoutManager as LinearLayoutManager
        val first = manager.findFirstVisibleItemPosition()
        val last = manager.findLastVisibleItemPosition()
        val count = manager.itemCount
        when {
            first <= 0 -> {
                onScrolledToTop()
            }
            last >= count - 1 -> {
                onScrolledToBottom()
            }
        }
    }

    abstract fun onScrolledToTop()

    abstract fun onScrolledToBottom()
}
