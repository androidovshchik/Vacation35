package rf.vacation35.inject

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import rf.vacation35.remote.DbApi

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    fun provideDbApi(): DbApi {
        return DbApi.getInstance()
    }
}
