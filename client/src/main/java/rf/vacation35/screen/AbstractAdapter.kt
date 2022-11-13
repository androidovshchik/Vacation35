package rf.vacation35.screen

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

inline fun <V : ViewBinding, T> abstractAdapter(
    items: List<T> = mutableListOf(),
    body: AbstractAdapter<V, T>.() -> Unit
): AbstractAdapter<V, T> {
    return AbstractAdapter<V, T>(items.toMutableList()).apply { body() }
}

class AbstractAdapter<V : ViewBinding, T>(val items: MutableList<T>) : RecyclerView.Adapter<AbstractAdapter.ViewHolder<V>>() {

    private lateinit var _onCreateViewHolder: (parent: ViewGroup) -> V
    private var _onBindViewHolder: (binding: V, item: T) -> Unit = { _, _ -> }

    fun onCreateViewHolder(body: (parent: ViewGroup) -> V) {
        _onCreateViewHolder = body
    }

    fun onBindViewHolder(body: (binding: V, item: T) -> Unit) {
        _onBindViewHolder = body
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<V> {
        return ViewHolder(_onCreateViewHolder(parent))
    }

    override fun onBindViewHolder(holder: ViewHolder<V>, position: Int) {
        val item = items[position]
        _onBindViewHolder(holder.binding, item)
    }

    override fun getItemCount(): Int = items.count()

    class ViewHolder<V : ViewBinding>(val binding: V) : RecyclerView.ViewHolder(binding.root)
}
