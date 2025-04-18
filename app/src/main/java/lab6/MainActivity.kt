package lab6

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import lab6.ui.theme.Lab6Theme
import java.time.LocalDate
import com.example.lab2.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import lab6.data.AppContainer
import lab6.data.CurrentDateProvider
import lab6.data.LocalDateConverter
import lab6.data.LocalDateProvider
import lab6.data.TodoApplication
import lab6.data.TodoTask
import lab6.data.TodoTaskRepository
import java.time.Instant
import java.time.ZoneId
import java.util.jar.Manifest

const val notificationID = 121
const val channelID = "Lab06 channel"
const val titleExtra = "title"
const val messageExtra = "message"

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel()
        container = (this.application as TodoApplication).container
        scheduleAlarm(2000)

        enableEdgeToEdge()
        setContent {
            Lab6Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun MainScreen(context: TodoApplication? = null) {
        val navController = rememberNavController()
        val postNotificationPermission =
            rememberPermissionState(permission = android.Manifest.permission.POST_NOTIFICATIONS)
        LaunchedEffect(key1 = true) {
            if (!postNotificationPermission.status.isGranted) {
                postNotificationPermission.launchPermissionRequest()
            }
        }
        NavHost(navController = navController, startDestination = "list") {
            composable("list") { ListScreen(navController = navController) }
            composable("form") { FormScreen(navController = navController) }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun MainScreenPreview() {
        Lab6Theme {
            MainScreen(
            )
        }
    }

    fun scheduleAlarm(time: Long) {
        val intent = Intent(applicationContext, NotificationBroadcastReceiver::class.java)
        intent.putExtra(titleExtra, "Deadline")
        intent.putExtra(messageExtra, "Zbliża się termin zakończenia zadania")

        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            notificationID,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            time,
            pendingIntent
        )
    }

    private fun createNotificationChannel() {
        val name = "Lab06 channel"
        val descriptionText = "Lab06 is channel for notifications for approaching tasks."
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        lateinit var container: AppContainer
    }
}

class NotificationHandler(private val context: Context) {
    private val notificationManager =
        context.getSystemService(NotificationManager::class.java)

    fun showSimpleNotification() {
        val notification = NotificationCompat.Builder(context, channelID)
            .setContentTitle("Proste powiadomienie")
            .setContentText("Tekst powiadomienia")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationManager.IMPORTANCE_HIGH)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(notificationID, notification)
    }
}

class NotificationBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val notification = NotificationCompat.Builder(context, channelID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(intent?.getStringExtra(titleExtra))
            .setContentText(intent?.getStringExtra(messageExtra))
            .build()
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationID, notification)
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!", modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Lab6Theme {
        Greeting("Android")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    navController: NavController,
    viewModel: ListViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val listUiState by viewModel.listUiState.collectAsState()

    Scaffold(floatingActionButton = {
        FloatingActionButton(shape = CircleShape, content = {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add task",
                modifier = Modifier.scale(1.5f)
            )
        }, onClick = {
            navController.navigate("form")
        })
    }, topBar = {
        AppTopBar(
            navController = navController, title = "List", showBackIcon = false, route = "list"
        )
    }, content = {
        LazyColumn(modifier = Modifier.padding(it)) {
            items(listUiState.items.size) { item ->
                ListItem(item = listUiState.items[item])
            }
        }
    })
}

class FormViewModel(
    private val repository: TodoTaskRepository,
    private val dateProvider: LocalDateProvider
) : ViewModel() {

    var todoTaskUiState by mutableStateOf(TodoTaskUiState())
        private set

    suspend fun save() {
        if (validate()) {
            repository.insertItem(todoTaskUiState.todoTask.toTodoTask())
        }
    }

    fun updateUiState(todoTaskForm: TodoTaskForm) {
        todoTaskUiState = TodoTaskUiState(todoTask = todoTaskForm, isValid = validate(todoTaskForm))
    }

    private fun validate(uiState: TodoTaskForm = todoTaskUiState.todoTask): Boolean {
        return with(uiState) {
            val date = dateProvider.getDate()
            val instant = date.atStartOfDay(ZoneId.systemDefault()).toInstant()
            val millis = instant.toEpochMilli()

            title.isNotBlank() &&
                    deadline > millis
        }
    }
}

data class TodoTaskUiState(
    var todoTask: TodoTaskForm = TodoTaskForm(),
    val isValid: Boolean = false
)

data class TodoTaskForm(
    val id: Int = 0,
    val title: String = "",
    val deadline: Long = LocalDateConverter.toMillis(LocalDate.now()),
    val isDone: Boolean = false,
    val priority: String = Priority.Low.name
)

fun TodoTask.toTodoTaskUiState(isValid: Boolean = false): TodoTaskUiState = TodoTaskUiState(
    todoTask = this.toTodoTaskForm(),
    isValid = isValid
)

fun TodoTaskForm.toTodoTask(): TodoTask = TodoTask(
    id = id,
    title = title,
    deadline = LocalDateConverter.fromMillis(deadline),
    isDone = isDone,
    priority = Priority.valueOf(priority)
)

fun TodoTask.toTodoTaskForm(): TodoTaskForm = TodoTaskForm(
    id = id,
    title = title,
    deadline = LocalDateConverter.toMillis(deadline),
    isDone = isDone,
    priority = priority.name
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoTaskInputForm(
    item: TodoTaskForm,
    modifier: Modifier = Modifier,
    onValueChange: (TodoTaskForm) -> Unit = {},
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = item.title,
        onValueChange = {
            onValueChange(item.copy(title = it))
        },
        label = { Text("Title") }
    )
    val datePickerState = rememberDatePickerState(
        initialDisplayMode = DisplayMode.Picker,
        yearRange = IntRange(2000, 2030),
        initialSelectedDateMillis = item.deadline,
    )
    var showDialog by remember {
        mutableStateOf(false)
    }
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = {
                showDialog = true
            }),
        text = "Date",
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.headlineMedium
    )
    if (showDialog) {
        DatePickerDialog(
            onDismissRequest = {
                showDialog = false
            },
            confirmButton = {
                Button(onClick = {
                    showDialog = false
                    onValueChange(item.copy(deadline = datePickerState.selectedDateMillis!!))
                }) {
                    Text("Pick")
                }
            }
        ) {
            DatePicker(state = datePickerState, showModeToggle = true)
        }
    }

    var selectedPriority by remember { mutableStateOf(Priority.Low) }

    Column(
        horizontalAlignment = Alignment.Start
    ) {
        for (prio in Priority.entries) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedPriority == prio,
                    onClick = {
                        selectedPriority =
                            prio; onValueChange(item.copy(priority = selectedPriority.name))
                    }
                )
                Text(
                    text = prio.name,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun TodoTaskInputBody(
    todoUiState: TodoTaskUiState,
    onItemValueChange: (TodoTaskForm) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TodoTaskInputForm(
            item = todoUiState.todoTask,
            onValueChange = onItemValueChange,
            modifier = modifier
        )
    }
}

data class FormState(val title: String, val priority: Priority, val date: LocalDate)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(
    navController: NavController,
    viewModel: FormViewModel = viewModel(factory = FormViewModelProvider.Factory)
) {
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            AppTopBar(
                navController = navController,
                title = "Form",
                showBackIcon = true,
                route = "form",
                onSaveClick = {
                    coroutineScope.launch {
                        viewModel.save()
                        navController.navigate("list")
                    }
                }
            )
        }
    )
    {
        TodoTaskInputBody(
            todoUiState = viewModel.todoTaskUiState,
            onItemValueChange = viewModel::updateUiState,
            modifier = Modifier.padding(it)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    navController: NavController,
    title: String,
    showBackIcon: Boolean,
    route: String,
    onSaveClick: () -> Unit = {}
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary
        ), title = { Text(text = title) }, navigationIcon = {
            if (showBackIcon) {
                IconButton(onClick = { navController.navigate("list") }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack, contentDescription = "Back"
                    )
                }
            }
        }, actions = {
            if (route == "form") {
                OutlinedButton(
                    onClick = {
                        onSaveClick()
                    }
                ) {
                    Text(
                        text = "Zapisz", fontSize = 18.sp
                    )
                }
            } else {
                IconButton(onClick = {
                    MainActivity.container.notificationHandler.showSimpleNotification()
                }) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = "")
                }
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(imageVector = Icons.Default.Home, contentDescription = "")
                }
            }
        })
}

@Composable
fun ListItem(item: TodoTask, modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(120.dp)
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(Modifier.padding(horizontal = 1.dp)) {
            Column(Modifier.padding(horizontal = 3.dp)) {
                Text(text = item.title)
                Text(text = "Priority: ${item.priority}")
            }
            Column(Modifier.padding(horizontal = 3.dp)) {
                var iconTint: Color
                var iconResource: Int
                if (item.isDone) {
                    iconResource = R.drawable.baseline_done_24
                    iconTint = Color(0, 255, 0, 255)
                } else {
                    iconResource = R.drawable.baseline_not_done_24
                    iconTint = Color(255, 0, 0, 255)
                }

                Icon(
                    painter = painterResource(iconResource),
                    contentDescription = "isDone",
                    tint = iconTint
                )
            }
            Column(Modifier.padding(horizontal = 3.dp)) {
                Text(text = "Deadline: ${item.deadline}")
            }
        }
    }
}

class ListViewModel(val repository: TodoTaskRepository) : ViewModel() {
    val listUiState: StateFlow<ListUiState>
        get() {
            return repository.getAllAsStream().map { ListUiState(it) }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                    initialValue = ListUiState()
                )
        }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class ListUiState(val items: List<TodoTask> = listOf())

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            ListViewModel(
                repository = todoApplication().container.todoTaskRepository
            )
        }
    }
}

object FormViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            FormViewModel(
                repository = todoApplication().container.todoTaskRepository,
                dateProvider = CurrentDateProvider()
            )
        }
    }
}

fun CreationExtras.todoApplication(): TodoApplication {
    val app = this[APPLICATION_KEY]
    return app as TodoApplication
}

val todoTasks = mutableListOf(
    TodoTask(0, "Programming", LocalDate.of(2024, 4, 18), false, Priority.Low),
    TodoTask(1, "Teaching", LocalDate.of(2024, 5, 12), false, Priority.High),
    TodoTask(2, "Learning", LocalDate.of(2024, 6, 28), true, Priority.Low),
    TodoTask(3, "Cooking", LocalDate.of(2024, 8, 18), false, Priority.Medium),
)

enum class Priority() {
    Low, Medium, High
}
