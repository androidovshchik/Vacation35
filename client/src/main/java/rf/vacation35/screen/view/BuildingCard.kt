package rf.vacation35.screen.view

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.card.MaterialCardView
import rf.vacation35.R
import rf.vacation35.databinding.MergeBuildingBinding
import rf.vacation35.extension.dp
import splitties.systemservices.layoutInflater

class BuildingCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.materialCardViewStyle
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val binding = MergeBuildingBinding.inflate(context.layoutInflater, this)

    init {
        cardElevation = dp(36)
    }
}
