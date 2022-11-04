package rf.vacation35.inject

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.qualifiers.ActivityContext

@Module
@InstallIn(FragmentComponent::class)
class FragmentModule {

    @Provides
    fun provideProgressDialog(@ActivityContext context: Context): ProgressDialog {
        val dialog = ProgressDialog(context as Activity)
        dialog.setTitle("Пожалуйста, подождите...")
        dialog.setMessage(null)
        dialog.isIndeterminate = true
        dialog.setCancelable(false)
        dialog.setOnCancelListener(null)
        return dialog
    }
}
