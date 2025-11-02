# CS 501 Assignment 5 Question 2 — MyDailyHub

## Overview
**MyDailyHub** is a 3-screen Android app built with **Jetpack Compose** that lets users manage daily notes and tasks while viewing a simple calendar placeholder.  
It demonstrates **Bottom Navigation**, **Compose Navigation**, **state management via ViewModel**, and **animated screen transitions using navigation arguments**.

## Features
- **Notes Screen:** Add and view short text notes. Uses a `ViewModel` to store notes persistently in memory.  
- **Tasks Screen:** Create a checklist with togglable tasks using `Checkbox`.  
- **Calendar Screen:** A simple static placeholder screen to demonstrate navigation structure.  
- **Bottom Navigation:** Built with `BottomNavigation` and `BottomNavigationItem` to navigate between screens.  
- **Backstack Management:** Uses `popUpTo()`, `launchSingleTop`, and `restoreState = true` to prevent duplicate screens and maintain state.  
- **Screen Animation:** Uses `Crossfade()` triggered by a dynamic query parameter (`?anim=token`) for smooth transitions.

## How to Run
1. Open the project in **Android Studio**.  
2. Make sure your `app/build.gradle.kts` file includes these dependencies:
   ```kotlin
   implementation("androidx.navigation:navigation-compose:2.8.3")
   implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
   implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")
   ```
3. Sync Gradle and run the app on an emulator or physical device.

### App Flow
1. Tap a bottom navigation icon to switch between **Notes**, **Tasks**, or **Calendar**.  
2. Each tab remembers its state — switching tabs doesn’t reset entered text or list data.  
3. Smooth **Crossfade animations** occur between screens using the dynamic navigation parameter.  

### Back Button Behavior
- **Back from a top-level tab** exits the app if no deeper routes were visited.  
- **Switching tabs** does not duplicate screens because of `launchSingleTop`.  
- **State restoration** is enabled via `restoreState = true`, so each screen’s state persists even after navigating away.

### Architecture
| Component | Description |
|------------|-------------|
| `NotesViewModel` | Manages and stores all note entries in memory. |
| `TasksViewModel` | Manages task list and checkbox state with a mutable list. |
| `Route` | Sealed class that defines navigation destinations and appends a unique token for animation. |
| `BottomBar()` | Implements dynamic highlighting using `currentBackStackEntryAsState()`. |
| `NavHost` | Connects screens and handles navigation transitions. |

## AI Usage Documentation

### How AI Was Used
- **Fixed animation behavior:** Initially, screen transitions didn’t animate because the `anim` argument was static. AI suggested adding a dynamic `token` parameter (timestamp) to force `Crossfade()` to trigger properly:
  ```kotlin
  val token = System.currentTimeMillis().toString()
  nav.navigate(route.path(animToken = token)) { ... }
  ```
- **Navigation cleanup:** AI clarified that `navArgument` was unnecessary for reading optional query arguments, since they can be retrieved directly using:
  ```kotlin
  val animFlag = entry.arguments?.getString("anim") ?: ""
  ```
- **Ensured backstack control:** Verified correct use of `launchSingleTop`, `popUpTo(nav.graph.findStartDestination().id)`, and `restoreState = true` to preserve screen state.

### Where AI Misunderstood
AI initially assumed static navigation arguments (`?anim=1`) would trigger animations, but `Crossfade()` only reacts when the target state changes. After clarification, a **dynamic token** was introduced to make each navigation event unique, successfully fixing the animation issue.
