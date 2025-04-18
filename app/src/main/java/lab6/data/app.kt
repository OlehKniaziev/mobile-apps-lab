package lab6.data

import android.app.Application
import android.content.Context
import lab6.NotificationHandler
import java.time.LocalDate

interface CurrentDateProvider {
    fun getDate(): LocalDate
}

class LocalDateProvider : CurrentDateProvider {
    override fun getDate(): LocalDate = LocalDate.now()
}

interface AppContainer {
    val todoTaskRepository: TodoTaskRepository
    val notificationHandler: NotificationHandler
}

class AppDataContainer(private val context: Context): AppContainer{
    override val todoTaskRepository: TodoTaskRepository by lazy{
        DatabaseTodoTaskRepository(AppDatabase.getInstance(context).taskDao())
    }

    override val notificationHandler: NotificationHandler by lazy {
        NotificationHandler(context)
    }
}

class TodoApplication : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this.applicationContext)
    }
}