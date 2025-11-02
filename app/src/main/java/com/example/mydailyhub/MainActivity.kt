package com.example.mydailyhub

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlin.math.max

/* ------------------------------- Routes ---------------------------------- */

sealed class Route(val base: String) {
    object Notes : Route("notes")
    object Tasks : Route("tasks")
    object Calendar : Route("calendar")

    // Append a unique token to force Crossfade when switching tabs
    fun path(animToken: String? = null): String =
        if (animToken != null) "$base?anim=$animToken" else base
}

/* ------------------------------- ViewModels ------------------------------ */

class NotesViewModel : ViewModel() {
    private val _notes = mutableStateListOf("Buy milk", "Read an article", "Plan weekend")
    val notes: List<String> get() = _notes
    fun addNote(text: String) {
        if (text.isNotBlank()) _notes.add(text.trim())
    }
}

data class Task(val id: Int, val text: String, val done: Boolean)

class TasksViewModel : ViewModel() {
    private val _tasks = mutableStateListOf(
        Task(1, "Finish assignment", false),
        Task(2, "Register for classes", true),
        Task(3, "Walk 20 minutes", false)
    )
    val tasks: List<Task> get() = _tasks
    private var nextId = (_tasks.maxOfOrNull { it.id } ?: 0) + 1

    fun addTask(text: String) {
        if (text.isBlank()) return
        val id = max(nextId, 1)
        _tasks.add(Task(id, text.trim(), false))
        nextId = id + 1
    }

    fun toggle(id: Int) {
        val idx = _tasks.indexOfFirst { it.id == id }
        if (idx >= 0) {
            val t = _tasks[idx]
            _tasks[idx] = t.copy(done = !t.done)
        }
    }
}

/* ------------------------------- Activity -------------------------------- */

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { AppRoot() }
    }
}

/* --------------------------------- App ----------------------------------- */

@Composable
fun AppRoot() {
    val nav = rememberNavController()

    // Observe current route for title and bottom-bar highlighting
    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route ?: Route.Notes.base

    val title = when {
        currentRoute.startsWith(Route.Notes.base) -> "Notes"
        currentRoute.startsWith(Route.Tasks.base) -> "Tasks"
        else -> "Calendar"
    }

    MaterialTheme {
        Scaffold(
            topBar = { TopAppBar(title = { Text(title) }) },
            bottomBar = { BottomBar(nav) }
        ) { inner ->
            NavHost(
                navController = nav,
                startDestination = Route.Notes.base,
                modifier = Modifier.padding(inner)
            ) {
                // Read query param 'anim' directly; no navArgument needed
                composable("${Route.Notes.base}?anim={anim}") { entry ->
                    val animFlag = entry.arguments?.getString("anim") ?: ""
                    NotesScreen(animFlag)
                }
                composable("${Route.Tasks.base}?anim={anim}") { entry ->
                    val animFlag = entry.arguments?.getString("anim") ?: ""
                    TasksScreen(animFlag)
                }
                composable("${Route.Calendar.base}?anim={anim}") { entry ->
                    val animFlag = entry.arguments?.getString("anim") ?: ""
                    CalendarScreen(animFlag)
                }
            }
        }
    }
}

/* --------------------------- Bottom Navigation --------------------------- */

@Composable
fun BottomBar(nav: NavHostController) {
    val items = listOf(Route.Notes, Route.Tasks, Route.Calendar)
    val backStack by nav.currentBackStackEntryAsState()
    val dest: NavDestination? = backStack?.destination

    BottomNavigation {
        items.forEach { route ->
            val selected = dest?.route?.startsWith(route.base) == true
            BottomNavigationItem(
                selected = selected,
                onClick = {
                    // Use a fresh token so Crossfade targetState changes every tap
                    val token = System.currentTimeMillis().toString()
                    nav.navigate(route.path(animToken = token)) {
                        // Backstack & state rules for top-level tabs
                        launchSingleTop = true
                        popUpTo(nav.graph.findStartDestination().id) {
                            saveState = true
                        }
                        restoreState = true
                    }
                },
                icon = {
                    when (route) {
                        Route.Notes -> Icon(Icons.Filled.Edit, contentDescription = "Notes")
                        Route.Tasks -> Icon(Icons.Filled.List, contentDescription = "Tasks")
                        Route.Calendar -> Icon(Icons.Filled.Event, contentDescription = "Calendar")
                    }
                },
                label = {
                    Text(
                        when (route) {
                            Route.Notes -> "Notes"
                            Route.Tasks -> "Tasks"
                            Route.Calendar -> "Calendar"
                        }
                    )
                }
            )
        }
    }
}

/* ------------------------------- Screens --------------------------------- */

@SuppressLint("UnusedCrossfadeTargetStateParameter")
@Composable
fun NotesScreen(animKey: String, vm: NotesViewModel = viewModel()) {
    var input by remember { mutableStateOf("") }

    // Crossfade re-runs when animKey changes (token passed in nav route)
    Crossfade(targetState = animKey, modifier = Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Notes", style = MaterialTheme.typography.h6, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text("Add note") },
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = { vm.addNote(input); input = "" }) { Text("Add") }
            }
            Divider()
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(vm.notes) { note ->
                    Surface(elevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                        Text(note, modifier = Modifier.padding(12.dp))
                    }
                }
            }
        }
    }
}

@SuppressLint("UnusedCrossfadeTargetStateParameter")
@Composable
fun TasksScreen(animKey: String, vm: TasksViewModel = viewModel()) {
    var input by remember { mutableStateOf("") }

    Crossfade(targetState = animKey, modifier = Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Tasks", style = MaterialTheme.typography.h6, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text("New task") },
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = { vm.addTask(input); input = "" }) { Text("Add") }
            }
            Divider()
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(vm.tasks, key = { it.id }) { task ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = task.done, onCheckedChange = { vm.toggle(task.id) })
                        Text(text = task.text, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        }
    }
}

@SuppressLint("UnusedCrossfadeTargetStateParameter")
@Composable
fun CalendarScreen(animKey: String) {
    Crossfade(targetState = animKey, modifier = Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Calendar", style = MaterialTheme.typography.h6, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Text("Static placeholder")
            }
        }
    }
}

/* -------------------------------- Preview -------------------------------- */

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewApp() {
    AppRoot()
}