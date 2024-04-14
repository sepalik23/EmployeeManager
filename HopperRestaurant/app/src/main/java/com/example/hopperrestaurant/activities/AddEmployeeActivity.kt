package com.example.hopperrestaurant.activities

import android.app.AlertDialog
import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.util.Patterns
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.example.hopperrestaurant.DBHelper
import com.example.hopperrestaurant.EmpAvailability
import com.example.hopperrestaurant.Employee
import com.example.hopperrestaurant.R
import com.example.hopperrestaurant.databinding.ActivityAddEmployeeBinding

class AddEmployeeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddEmployeeBinding
    private lateinit var dbHelper: DBHelper
    private lateinit var addBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEmployeeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dbHelper = DBHelper(this)
        addBtn = binding.addBtn

        // set title of action bar
        supportActionBar?.title = "Add New Employee"
        // show back button. On back button click, onOptionsItemSelected() will be called
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // this makes the phone number automatically get formatted like (204) 698-4178
        binding.phoneNumEditText.addTextChangedListener(PhoneNumberFormattingTextWatcher())
        // disable the add button till the form is valid
        addBtn.isEnabled = false

        // start validations, the button will be re enabled when everything is valid
        firstNameEditTextListener()
        lastNameEditTextListener()
        nickNameEditTextListener()
        emailEditTextListener()
        phoneNumEditTextListener()

        // Drop down Spinners for days
        initSpinner(binding.monSpinner)
        initSpinner(binding.tueSpinner)
        initSpinner(binding.wedSpinner)
        initSpinner(binding.thurSpinner)
        initSpinner(binding.friSpinner)
        initSpinner(binding.satSpinner, true)
        initSpinner(binding.sunSpinner, true)
        // End of spinners

        addBtn.setOnClickListener {
            if (allAvailAreNone()) { // All availabilities are none, ask for confirmation
                val builder = AlertDialog.Builder(this)
                    .setTitle("Confirmation")
                    .setMessage("Are you sure you want to add this employee with no availability?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setIconAttribute(android.R.attr.alertDialogIcon)
                    .setPositiveButton("Yes") { _, _ ->
                        addEmployee()
                        finish()
                    }
                    .setNegativeButton("No") { _, _ ->
                        Toast.makeText(this, "Employee was not added", Toast.LENGTH_SHORT).show()
                    }
                builder.show()

            } else { // at least one availability was set, just save
                addEmployee()
                finish()
            }
        }
    }

    // Returns true if all availabilities are set to none, false if not
    private fun allAvailAreNone(): Boolean {
        val monSpinner = binding.monSpinner.selectedItem.toString()
        val tueSpinner = binding.tueSpinner.selectedItem.toString()
        val wedSpinner = binding.wedSpinner.selectedItem.toString()
        val thurSpinner = binding.thurSpinner.selectedItem.toString()
        val friSpinner = binding.friSpinner.selectedItem.toString()
        val satSpinner = binding.satSpinner.selectedItem.toString()
        val sunSpinner = binding.sunSpinner.selectedItem.toString()

        if (monSpinner == "None" && tueSpinner == "None" && wedSpinner == "None" && thurSpinner == "None"
            && friSpinner == "None" && satSpinner == "None" && sunSpinner == "None"
        ) {
            return true
        }
        return false
    }

    private fun initSpinner(spinner: Spinner, isWeekend: Boolean = false) {
        var array = R.array.avail_array
        if (isWeekend) {
            array = R.array.weekend_avail_array
        }
        ArrayAdapter.createFromResource(
            this,
            array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * This methods adds the employee to the database, and returns true if the employee was added
     * or false if not
     * */
    private fun addEmployee() {
        // first, add employee
        val employee =
            getEmpFromBinding() // get the employee, like binding.editText.text.toString()
        val empId = dbHelper.addEmployee(employee)
        // employee has been added, now add their availability
        val avail = getAvailFromBinding(empId) // same as above
        dbHelper.addAvailability(avail)
        Toast.makeText(this, "EMPLOYEE ADDED SUCCESSFULLY", Toast.LENGTH_SHORT).show()
    }

    private fun getEmpFromBinding(): Employee {
        return Employee(
            -1L,
            binding.firstNameEditText.text.toString(),
            binding.lastNameEditText.text.toString(),
            binding.nickNameEditText.text.toString(),
            binding.emailEditText.text.toString(),
            binding.phoneNumEditText.text.toString(),
            binding.canOpenCheckBox.isChecked,
            binding.cacCloseCheckBox.isChecked,
            true,
        )
    }

    private fun getAvailFromBinding(empId: Long): EmpAvailability {
        val mon = binding.monSpinner.selectedItem.toString()
        val tue = binding.tueSpinner.selectedItem.toString()
        val wed = binding.wedSpinner.selectedItem.toString()
        val thur = binding.thurSpinner.selectedItem.toString()
        val fri = binding.friSpinner.selectedItem.toString()
        val sat = binding.satSpinner.selectedItem.toString()
        val sun = binding.sunSpinner.selectedItem.toString()

        return EmpAvailability(
            -1L,
            empId,
            mon == "AM" || mon == "Full",
            mon == "PM" || mon == "Full",
            tue == "AM" || tue == "Full",
            tue == "PM" || tue == "Full",
            wed == "AM" || wed == "Full",
            wed == "PM" || wed == "Full",
            thur == "AM" || thur == "Full",
            thur == "PM" || thur == "Full",
            fri == "AM" || fri == "Full",
            fri == "PM" || fri == "Full",
            sat == "Full",
            sun == "Full"
        )
    }

    /**
     * This method enables the add employee button if all the form data is good
     */
    private fun enableButtonIfValid() {
        // The form is valid if the validate methods returns null
        val formIsValid = validateName("First") == null &&
                validateName("Last") == null &&
                validateNickname() == null &&
                validateEmail() == null &&
                validatePhoneNum() == null

        addBtn.isEnabled = formIsValid
    }

    /**
     * For all the Listener methods:
     *
     * They all check if the field is valid as each letter/number is being typed (doOnTextChanged)
     * and as the user taps outside the field (setOnFocusChangeListener)
     * */
    private fun firstNameEditTextListener() {
        // as the user types each letter, check if the field is valid
        binding.firstNameEditText.doOnTextChanged { _, _, _, _ ->
            binding.firstNameContainer.helperText = validateName("First")
            enableButtonIfValid()
        }
        // when the user leaves the edit text, check if the field is valid
        binding.firstNameEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                binding.firstNameContainer.helperText = validateName("First")
                enableButtonIfValid()
            }
        }
    }

    private fun lastNameEditTextListener() {
        // as the user types each letter, check if the field is valid
        binding.lastNameEditText.doOnTextChanged { _, _, _, _ ->
            binding.lastNameContainer.helperText = validateName("Last")
            enableButtonIfValid()
        }
        // when the user leaves the edit text, check if the field is valid
        binding.lastNameEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                binding.lastNameContainer.helperText = validateName("Last")
                enableButtonIfValid()
            }
        }
    }

    private fun nickNameEditTextListener() {
        // as the user types each letter, check if the field is valid
        binding.nickNameEditText.doOnTextChanged { _, _, _, _ ->
            binding.nickNameContainer.helperText = validateNickname()
            enableButtonIfValid()
        }
        // when the user leaves the edit text, check if the field is valid
        binding.nickNameEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                binding.nickNameContainer.helperText = validateNickname()
                enableButtonIfValid()
            }
        }
    }

    private fun phoneNumEditTextListener() {
        // as the user types each letter, check if the field is valid
        binding.phoneNumEditText.doOnTextChanged { _, _, _, _ ->
            binding.phoneNumContainer.helperText = validatePhoneNum()
            enableButtonIfValid()
        }
        // when the user leaves the edit text, check if the field is valid
        binding.phoneNumEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                binding.phoneNumContainer.helperText = validatePhoneNum()
                enableButtonIfValid()
            }
        }
    }

    private fun emailEditTextListener() {
        // as the user types each letter, check if the field is valid
        binding.emailEditText.doOnTextChanged { _, _, _, _ ->
            binding.emailContainer.helperText = validateEmail()
            enableButtonIfValid()
        }
        // when the user leaves the edit text, check if the field is valid
        binding.emailEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                binding.emailContainer.helperText = validateEmail()
                enableButtonIfValid()
            }
        }
    }

    /**
     * Depending on the parameters passed, this method checks if the first or last name is valid
     * For our purposes, the first/last name is valid if it's not empty
     *
     * If not valid, this method returns an error message. Else, returns null
     */
    private fun validateName(firstOrLast: String): String? {
        val name =
            if (firstOrLast == "First") {
                binding.firstNameEditText.text.toString()
            } else {
                binding.lastNameEditText.text.toString()
            }

        if (name.isEmpty() || name.isBlank()) {
            return "$firstOrLast name is required"
        }
        // returns null if name doesn't contain special characters
        return rejectSpecialCharacters(name)
    }

    private fun validateNickname(): String? {
        val nickname = binding.nickNameEditText.text.toString()
        if (nickname.isEmpty() || nickname.isBlank()) {
            return null
        }
        // we don't need to check if the nickname is empty, because it's not a required field
        // so only check if it contains special characters
        // returns null if nickname doesn't contain special characters
        return rejectSpecialCharacters(nickname)
    }

    private fun rejectSpecialCharacters(string: String): String? {
        val hyphenCount = string.count { it == '-' }
        val apostropheCount = string.count { it == '\'' }

        if (hyphenCount == 1 || apostropheCount == 1) {
            return null
        }

        if (!string.matches(Regex("[a-zA-Z]+"))) {
            // add all none letters, ignore apostrophes and hyphens
            val illegalChar = string.filter { !it.isLetter() }
            return "\"$illegalChar\" is not valid"
        }
        return null
    }

    /**
     * This method returns an error message if the email is not valid, or null if it's valid
     *
     * An email is not valid if it's empty, or doesn't match the email pattern, or if it already
     * exists
     * */
    private fun validateEmail(): String? {
        val email = binding.emailEditText.text.toString()

        if (email.isEmpty() || email.isBlank()) {
            return "Email is required"
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "Invalid Email Address"
        }
        if (dbHelper.emailExists(email)) {
            return "That email already exists"
        }
        return null
    }

    /**
     * This method returns an error message if the phone number is not valid, or null if it's valid
     *
     * A phone number is not valid if it's empty, or doesn't match the phone number pattern, if it's
     * less than 14 characters (with formatting) or if it already exists
     * */
    private fun validatePhoneNum(): String? {
        val phoneNum = binding.phoneNumEditText.text.toString()

        if (phoneNum.isEmpty() || phoneNum.isBlank()) {
            return "Phone # is required"
        }
        if (!Patterns.PHONE.matcher(phoneNum).matches() || phoneNum.length != 14) {
            return "Invalid phone #"
        }
        if (dbHelper.phoneNumExists(phoneNum)) {
            return "Phone # already exists"
        }
        return null
    }
}
