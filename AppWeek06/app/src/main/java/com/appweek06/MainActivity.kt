package com.appweek06

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var radioGroup: RadioGroup
    private lateinit var radioStudentList: RadioButton
    private lateinit var radioShoppingCart: RadioButton
    private lateinit var radioTaskManager: RadioButton

    private lateinit var listView: ListView
    private lateinit var editTextInput: EditText
    private lateinit var buttonAdd: Button
    private lateinit var buttonClear: Button
    private lateinit var textViewInfo: TextView

    private lateinit var layoutCartControls: LinearLayout
    private lateinit var editTextPrice: EditText
    private lateinit var editTextQuantity: EditText

    private lateinit var layoutTaskControls: LinearLayout
    private lateinit var spinnerPriority: Spinner
    private lateinit var editTextDescription: EditText

    private lateinit var studentList: ArrayList<Student>
    private lateinit var taskList: ArrayList<Task>
    private lateinit var cartItemList: ArrayList<CartItem>

    private lateinit var studentAdapter: ArrayAdapter<Student>
    private lateinit var taskAdapter: ArrayAdapter<Task>
    private lateinit var cartAdapter: ArrayAdapter<CartItem>

    private var currentMode = AppMode.STUDENT_LIST

    companion object {
        private const val TAG = "KotlinWeek07App"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeData()
        setupViews()
        setupAdapters()
        setupListeners()

        setMode(AppMode.STUDENT_LIST)
        addInitialData()
    }

    private fun initializeData() {
        studentList = ArrayList()
        cartItemList = ArrayList()
        taskList = ArrayList()
    }

    private fun setupViews() {
        radioGroup = findViewById(R.id.radioGroup)
        radioStudentList = findViewById(R.id.radioStudentList)
        radioShoppingCart = findViewById(R.id.radioShoppingCart)
        radioTaskManager = findViewById(R.id.radioTaskManager)

        listView = findViewById(R.id.listView)
        editTextInput = findViewById(R.id.editTextInput)
        buttonAdd = findViewById(R.id.buttonAdd)
        buttonClear = findViewById(R.id.buttonClear)
        textViewInfo = findViewById(R.id.textViewInfo)

        layoutCartControls = findViewById(R.id.layoutCartControls)
        editTextPrice = findViewById(R.id.editTextPrice)
        editTextQuantity = findViewById(R.id.editTextQuantity)

        layoutTaskControls = findViewById(R.id.layoutTaskControls)
        spinnerPriority = findViewById(R.id.spinnerPriority)
        editTextDescription = findViewById(R.id.editTextDescription)

        setupPrioritySpinner()
    }

    private fun setupPrioritySpinner() {
        val priorities = TaskPriority.values().map { it.displayName }
        val priorityAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, priorities)
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPriority.adapter = priorityAdapter
    }

    private fun setupAdapters() {
        studentAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, studentList)
        cartAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, cartItemList)
        taskAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, taskList)
    }

    private fun setupListeners() {
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioStudentList -> setMode(AppMode.STUDENT_LIST)
                R.id.radioShoppingCart -> setMode(AppMode.SHOPPING_CART)
                R.id.radioTaskManager -> setMode(AppMode.TASK_MANAGER)
            }
        }

        buttonAdd.setOnClickListener { addItem() }
        buttonClear.setOnClickListener { clearAll() }

        listView.setOnItemClickListener { _, _, position, _ ->
            handleItemClick(position)
        }

        listView.setOnItemLongClickListener { _, _, position, _ ->
            handleItemLongClick(position)
            true
        }
    }

    private fun setMode(mode: AppMode) {
        currentMode = mode

        layoutCartControls.visibility = View.GONE
        layoutTaskControls.visibility = View.GONE

        when (mode) {
            AppMode.STUDENT_LIST -> {
                editTextInput.hint = "Enter student name"
                buttonAdd.text = "Add Student"
                listView.adapter = studentAdapter
            }

            AppMode.SHOPPING_CART -> {
                editTextInput.hint = "Enter item name"
                buttonAdd.text = "Add Item"
                layoutCartControls.visibility = View.VISIBLE
                listView.adapter = cartAdapter
                updateCartInfo()
            }

            AppMode.TASK_MANAGER -> {
                editTextInput.hint = "Enter task title"
                buttonAdd.text = "Add Task"
                layoutTaskControls.visibility = View.VISIBLE
                listView.adapter = taskAdapter
                updateTaskInfo()
            }
        }
        updateInfoDisplay()
    }

    private fun addItem() {
        val input = editTextInput.text.toString().trim()
        if (input.isEmpty()) {
            showToast("Please enter a value")
            return
        }

        when (currentMode) {
            AppMode.STUDENT_LIST -> addStudent(input)
            AppMode.SHOPPING_CART -> addCartItem(input)
            AppMode.TASK_MANAGER -> addTask(input)
        }

        editTextInput.text.clear()
        clearAdditionalFields()
        updateInfoDisplay()
    }

    private fun addStudent(name: String) {
        if (studentList.any { it.name == name }) {
            showToast("Student '$name' already exists")
            return
        }
        studentList.add(Student(name))
        studentAdapter.notifyDataSetChanged()
    }

    private fun addCartItem(name: String) {
        val price = editTextPrice.text.toString().toDoubleOrNull()
        val quantity = editTextQuantity.text.toString().toIntOrNull() ?: 1

        if (price == null || price <= 0) {
            showToast("Invalid price")
            return
        }

        val existing = cartItemList.find { it.name == name }
        if (existing != null) {
            existing.quantity += quantity
        } else {
            cartItemList.add(CartItem(name, quantity, price))
        }
        cartAdapter.notifyDataSetChanged()
        updateCartInfo()
    }

    private fun addTask(title: String) {
        val desc = editTextDescription.text.toString().trim()
        val priority = TaskPriority.values()[spinnerPriority.selectedItemPosition]
        val task = Task(title, desc, false, priority)
        taskList.add(task)
        taskAdapter.notifyDataSetChanged()
        updateTaskInfo()
    }

    private fun handleItemClick(position: Int) {
        when (currentMode) {
            AppMode.STUDENT_LIST -> showToast("Selected: ${studentList[position].name}")
            AppMode.SHOPPING_CART -> showItemDetailsDialog(cartItemList[position])
            AppMode.TASK_MANAGER -> toggleTaskCompletion(taskList[position])
        }
    }

    private fun handleItemLongClick(position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Delete Item")
            .setMessage("Are you sure you want to delete this item?")
            .setPositiveButton("Delete") { _, _ -> removeItem(position) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun removeItem(position: Int) {
        when (currentMode) {
            AppMode.STUDENT_LIST -> studentList.removeAt(position).also {
                studentAdapter.notifyDataSetChanged()
            }
            AppMode.SHOPPING_CART -> cartItemList.removeAt(position).also {
                cartAdapter.notifyDataSetChanged()
                updateCartInfo()
            }
            AppMode.TASK_MANAGER -> taskList.removeAt(position).also {
                taskAdapter.notifyDataSetChanged()
                updateTaskInfo()
            }
        }
        updateInfoDisplay()
    }

    private fun clearAll() {
        when (currentMode) {
            AppMode.STUDENT_LIST -> studentList.clear().also { studentAdapter.notifyDataSetChanged() }
            AppMode.SHOPPING_CART -> cartItemList.clear().also {
                cartAdapter.notifyDataSetChanged()
                updateCartInfo()
            }
            AppMode.TASK_MANAGER -> taskList.clear().also {
                taskAdapter.notifyDataSetChanged()
                updateTaskInfo()
            }
        }
        updateInfoDisplay()
    }

    private fun clearAdditionalFields() {
        editTextPrice.text.clear()
        editTextQuantity.text.clear()
        editTextDescription.text.clear()
        spinnerPriority.setSelection(0)
    }

    private fun showItemDetailsDialog(item: CartItem) {
        val msg = """
            Item: ${item.name}
            Quantity: ${item.quantity}
            Unit Price: $${String.format("%.2f", item.price)}
            Total: $${String.format("%.2f", item.getTotalPrice())}
            Added: ${SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault()).format(item.addedDate)}
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Item Details")
            .setMessage(msg)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun toggleTaskCompletion(task: Task) {
        task.isCompleted = !task.isCompleted
        taskAdapter.notifyDataSetChanged()
        updateTaskInfo()
        showToast("Task marked as ${if (task.isCompleted) "completed" else "pending"}")
    }

    private fun updateInfoDisplay() {
        when (currentMode) {
            AppMode.STUDENT_LIST -> textViewInfo.text = "Total Students: ${studentList.size}"
            AppMode.SHOPPING_CART -> updateCartInfo()
            AppMode.TASK_MANAGER -> updateTaskInfo()
        }
    }

    private fun updateCartInfo() {
        val totalItems = cartItemList.sumOf { it.quantity }
        val totalValue = cartItemList.sumOf { it.getTotalPrice() }
        textViewInfo.text = "Items: $totalItems | Total: $${String.format("%.2f", totalValue)}"
    }

    private fun updateTaskInfo() {
        val completed = taskList.count { it.isCompleted }
        val pending = taskList.size - completed
        val highPriority = taskList.count { it.priority == TaskPriority.HIGH && !it.isCompleted }
        textViewInfo.text = "Tasks: $pending pending, $completed completed | High: $highPriority"
    }

    private fun addInitialData() {
        studentList.addAll(listOf(Student("KIM"), Student("LEE"), Student("PARK")))
        cartItemList.addAll(listOf(CartItem("Apple", 3, 2.0), CartItem("Banana", 2, 1.0)))
        taskList.addAll(
            listOf(
                Task("Complete Assignment", "Mobile Programming", false, TaskPriority.HIGH),
                Task("Shopping", "Visit Mart", false, TaskPriority.MEDIUM),
                Task("Tour", "Museum", true, TaskPriority.LOW)
            )
        )
        studentAdapter.notifyDataSetChanged()
        cartAdapter.notifyDataSetChanged()
        taskAdapter.notifyDataSetChanged()
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}