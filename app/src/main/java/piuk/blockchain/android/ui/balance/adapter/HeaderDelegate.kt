package piuk.blockchain.android.ui.balance.adapter

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.item_accounts_row_header.view.*
import piuk.blockchain.android.R
import piuk.blockchain.android.ui.adapters.AdapterDelegate
import piuk.blockchain.android.util.extensions.gone
import piuk.blockchain.android.util.extensions.inflate

class HeaderDelegate<in T> : AdapterDelegate<List<T>> {

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder =
            HeaderViewHolder(parent.inflate(R.layout.item_accounts_row_header))

    override fun onBindViewHolder(items: List<T>, position: Int, holder: RecyclerView.ViewHolder, payloads: List<*>) {
        val headerViewHolder = holder as HeaderViewHolder
        headerViewHolder.header.text = items[position] as CharSequence?
    }

    override fun isForViewType(items: List<T>, position: Int): Boolean = items[position] is String

    private class HeaderViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal var header: TextView = itemView.header_name
        internal var button: ImageView = itemView.imageview_plus

        init {
            button.gone()
        }
    }
}