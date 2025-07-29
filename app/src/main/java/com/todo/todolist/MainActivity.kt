package com.todo.todolist

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.todo.todolist.data.Todo
import com.todo.todolist.databinding.ActivityMainBinding
import com.todo.todolist.databinding.AddFormBinding
import com.todo.todolist.firebaseAuth.AuthUtil
import com.todo.todolist.firebaseAuth.LoginActivity
import com.todo.todolist.worker.CleanupWorker
import com.todo.todolist.worker.ReminderWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var authUtil: AuthUtil

    private lateinit var binding: ActivityMainBinding
    private lateinit var cardAdapter: CardAdapter
    private val viewModel: TodoViewModel by viewModels()
    private val workRequest = PeriodicWorkRequestBuilder<CleanupWorker>(1, TimeUnit.DAYS).build()
    private val notificationRequest = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
        .setInitialDelay(1, TimeUnit.MINUTES)
        .build()
    private var currentFilter:(List<Todo>) -> List<Todo> ={it}

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        DarkTheme.applyTheme(this)

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        createNotificationChannel()
        val headerView = binding.navView.getHeaderView(0)
        val welcomeText= headerView.findViewById<TextView>(R.id.welcomeUser)

        val userId= FirebaseAuth.getInstance().currentUser?.uid
        if(userId != null) FirebaseFirestore.getInstance().collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val username = document.getString("username")
                welcomeText.text = "Welcome, $username"}


        cardAdapter = CardAdapter(object: TaskActionListener{
            override fun onEditTask(todo: Todo){
                showTaskFormDialog(todo)
            }
        }, viewModel)

        binding.recyclerview.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerview.adapter = cardAdapter
        lifecycleScope.launch { viewModel.allTodo.observeForever{todos-> cardAdapter.submitList(currentFilter(todos))} }

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "AutoCleanup", ExistingPeriodicWorkPolicy.KEEP, workRequest
        )

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "dailyReminder", ExistingPeriodicWorkPolicy.KEEP, notificationRequest
        )

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)

        binding.toolbar.setNavigationOnClickListener {
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.close()
            } else {
                binding.drawerLayout.open()
            }
        }

        viewModel.currentList.observe(this){todos -> cardAdapter.submitList(todos) }


        binding.navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> viewModel.showAll()
                R.id.nav_fav -> viewModel.showFavorites()
                R.id.nav_task -> viewModel.showCompleted()
                R.id.sort_date -> viewModel.sortByDate()
                R.id.sort_priority -> viewModel.sortByPriority()
                R.id.nav_group -> showCategoryDialog()
                R.id.dark_mode -> {
                    DarkTheme.toggleTheme(this)
                    recreate()
                }
                R.id.logout -> {
                    authUtil.signOut()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }
            binding.drawerLayout.closeDrawers()
            true
        }
        binding.createButton.setOnClickListener { showTaskFormDialog( null) }
    }

    private fun showCategoryDialog() {
        val categories = arrayOf("Personal", "Work", "Health","Shopping", "Others")
        AlertDialog.Builder(this)
            .setTitle("Choose Category")
            .setItems(categories) { _, i ->
                viewModel.filterByCategory(categories[i])
            }.show()
    }
    @SuppressLint("SetTextI18n")
    fun showTaskFormDialog(existingCard: Todo?) {

        val addFormBinding = AddFormBinding.inflate(layoutInflater)

        val calendar = Calendar.getInstance()

        val categories = arrayOf("Choose Category","Personal", "Work", "Health", "Shopping", "Others")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        addFormBinding.category.adapter = spinnerAdapter

        val priorities = arrayOf(
            "Choose Priority",
            "Low",
            "Medium",
            "High"
        )
        val spinnerPriorityAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, priorities)
        spinnerPriorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        addFormBinding.priority.adapter = spinnerPriorityAdapter

        if (DarkTheme.isDarkMode(this)) {
            addFormBinding.editContent.setEditorBackgroundColor(R.color.black)
            addFormBinding.editContent.setEditorFontColor(R.color.white)
        } else {
            addFormBinding.editContent.setEditorBackgroundColor(R.color.white)
            addFormBinding.editContent.setEditorFontColor(R.color.black)
        }

        existingCard?.let {
            addFormBinding.editTitle.setText(it.title)
            addFormBinding.editContent.html = it.content
            addFormBinding.startDate.setText(it.startDate)
            addFormBinding.EndDate.setText(it.endDate)
            val index = categories.indexOf(it.category)
            if (index != -1) addFormBinding.category.setSelection(index)
            val indexPri = priorities.indexOf(it.priority)
            if (indexPri != -1) addFormBinding.priority.setSelection(indexPri)
        }

        val redColor= ContextCompat.getColor(this, R.color.Red)

        // Rich text formatting buttons
        addFormBinding.boldButton.setOnClickListener { addFormBinding.editContent.setBold() }
        addFormBinding.italicButton.setOnClickListener { addFormBinding.editContent.setItalic() }
        addFormBinding.underlinedButton.setOnClickListener { addFormBinding.editContent.setUnderline() }
        addFormBinding.colorButton.setOnClickListener { addFormBinding.editContent.setTextColor(redColor) }
        addFormBinding.strikeThroughButton.setOnClickListener { addFormBinding.editContent.setStrikeThrough() }
        addFormBinding.BulletButton.setOnClickListener { addFormBinding.editContent.setBullets() }
        addFormBinding.checkboxButton.setOnClickListener { addFormBinding.editContent.insertTodo() }

        addFormBinding.editContent.setPlaceholder("Enter your task content here...")

        // Date picker setup
        fun showDatePicker(target: EditText): View.OnClickListener {
            return View.OnClickListener {
                val datePicker = DatePickerDialog(
                    this,
                    { _, year, month, day -> target.setText("$day/${month + 1}/$year") },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )
                datePicker.show()
            }
        }

        // Set date pickers before dialog is shown
        addFormBinding.startDate.setOnClickListener(showDatePicker(addFormBinding.startDate))
        addFormBinding.EndDate.setOnClickListener(showDatePicker(addFormBinding.EndDate))

        addFormBinding.editContent.setOnTextChangeListener{
            if (Html.fromHtml(it,Html.FROM_HTML_MODE_LEGACY).toString().trim().isNotEmpty()){
                addFormBinding.contentError.visibility = View.GONE
            }
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle(if (existingCard == null) "Add Task" else "Edit Task")
            .setView(addFormBinding.root)
            .setPositiveButton("Save", null) // We'll override this later
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            saveButton.setOnClickListener {

                val title = addFormBinding.editTitle.text.toString().trim()
                val contentHtml = addFormBinding.editContent.html?.toString()?.trim()?: ""
                val content = Html.fromHtml(contentHtml, Html.FROM_HTML_MODE_LEGACY).toString()
                val start = addFormBinding.startDate.text.toString().trim()
                val end = addFormBinding.EndDate.text.toString().trim()
                val priority = addFormBinding.priority.selectedItem.toString().trim()
                val category = addFormBinding.category.selectedItem.toString().trim()
                val priorityIcon= findViewById<ImageView>(R.id.priorityIcon)
                when {
                    title.isEmpty() -> {
                        addFormBinding.editTitle.error = "Please fill this field"
                        return@setOnClickListener
                    }

                    start.isEmpty() -> {
                        addFormBinding.startDate.error = "Please select a start Date"
                        return@setOnClickListener
                    }

                    end.isEmpty() -> {
                        addFormBinding.EndDate.error = "Please select a end Date"
                        return@setOnClickListener
                    }
                    content.isEmpty() -> {
                        addFormBinding.contentError.visibility = View.VISIBLE
                        addFormBinding.contentError.text="*Please enter task Content"
                        return@setOnClickListener
                    }
                    category == "Choose Category" ->{
                        addFormBinding.contentError.visibility = View.VISIBLE
                        addFormBinding.contentError.text="*Please Choose Category"
                        return@setOnClickListener
                    }
                    priority == "Choose Priority" ->{
                        addFormBinding.contentError.visibility = View.VISIBLE
                        addFormBinding.contentError.text="*Please Choose Priority"
                        return@setOnClickListener
                    }
                }
                val newCard = Todo(
                    id = existingCard?.id ?: 0,
                    userId = authUtil.getCurrentUserId(),
                    title = title,
                    content = content,
                    startDate = start,
                    endDate = end,
                    isFavorite = existingCard?.isFavorite ?: false,
                    isCompleted = existingCard?.isCompleted ?: false,
                    priority= priority,
                    category = category
                )
                if (existingCard == null) viewModel.insert(newCard)
                else viewModel.update(newCard)
                dialog.dismiss()
            }
        }
        dialog.show()
    }
    private fun createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val name = "Task Reminder"
            val descriptionText = "Reminder for your To-Do tasks"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("todo_channel", name, importance)
                .apply { description = descriptionText }
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

}




