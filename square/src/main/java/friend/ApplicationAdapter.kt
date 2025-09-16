package friend

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.corekit.recyclerview.SingleTypeAdapter
import com.example.corekit.recyclerview.ViewHolderTag
import com.jxdx.square.R
import entity.ApplicationMessage

class ApplicationAdapter(
    private val applicationList: List<ApplicationMessage>,
) : SingleTypeAdapter<ApplicationMessage>() {
    init {
        add(applicationList)
    }

    companion object {
        const val TYPE = 1
    }

    override fun createViewHolder(
        viewType: Int,
        inflater: LayoutInflater,
        parent: ViewGroup,
    ): RecyclerView.ViewHolder? =
        when (viewType) {
            TYPE -> {
                val view = inflater.inflate(R.layout.friend_request_item, parent, false)
                ApplicationViewHolder(view)
            }
            else -> null
        }

    class ApplicationViewHolder(
        private val view: View,
    ) : RecyclerView.ViewHolder(view),
        ViewHolderTag<ApplicationMessage> {
        override fun setHolder(entity: ApplicationMessage) {
            view.findViewById<View>(R.id.searchView)
        }
//        view.findViewById<View>(R.id.civ_comment_avatar)?.let {
//            if (it is androidx.appcompat.widget.AppCompatImageView) {
//                it.load(entity.avatarUrl, 1)
//            }
//        }

        override fun setHolder(
            entity: ApplicationMessage,
            payload: Any,
        ) {
            setHolder(entity)
        }
    }
}
