package com.example.hopperrestaurant.activities

import android.app.AlertDialog
import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.util.Patterns
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.example.hopperrestaurant.DBHelper
import com.example.hopperrestaurant.EmpAvailability
import com.example.hopperrestaurant.Employee
import com.example.hopperrestaurant.R
import com.example.hopperrestaurant.databinding.ActivityEditEmployeeBinding


class EditEmployeeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditEmployeeBinding
    private lateinit var dbHelper: DBHelper
    private lateinit var editBtn: Button
    private var curEmpId: Long = 0
    private lateinit var monSpinner: Spinner
    private lateinit var tueSpinner: Spinner
    private lateinit var wedSpinner: Spinner
    private lateinit var thuSpinner: Spinner
    private lateinit var friSpinner: Spinner
    private lateinit var satSpinner: Spinner
    private lateinit var sunSpinner: Spinner

    // these are used to check if the user has changed anything
    private lateinit var oldFirstName: String
    private lateinit var oldLastName: String
    private lateinit var oldNickname: String
    private lateinit var oldEmail: String
    private lateinit var oldPhoneNum: String
    private lateinit var oldMonSpinner: String
    private lateinit var oldTueSpinner: String
    private lateinit var oldWedSpinner: String
    private lateinit var oldThuSpinner: String
    private lateinit var oldFriSpinner: String
    private lateinit var oldSatSpinner: String
    private lateinit var oldSunSpinner: String
    private var oldCanOpen = false
    private var oldCanClose = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditEmployeeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dbHelper = DBHelper(this)
        editBtn = binding.editBtn
        curEmpId = intent.getLongExtra("curEmpId", 1L)

        val trashButton = findViewById<Button>(R.id.Delete_Button_Edit_Page)

        // set title of action bar
        supportActionBar?.title = "Edit Employee Information"
        // show back button. On back button click, onOptionsItemSelected() will be called
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        trashButton.setOnClickListener {

            val builder = AlertDialog.Builder(this)

            //dialog title
            builder.setTitle("Confirmation")

            //dialog message
            builder.setMessage("Are you sure you want to delete this employee?")
            builder.setIcon(android.R.drawable.ic_dialog_alert)

            //confirm delete button
            builder.setPositiveButton("Delete") { _, _ ->
                val deactivateEmp = dbHelper.deactivateEmployee(curEmpId)

                // calling deactivate function from DBHelper
                if (deactivateEmp == 1) {
                    Toast.makeText(applicationContext, "Employee Deleted", Toast.LENGTH_SHORT)
                        .show()
                    finish()
                }
                // this ACTION should NEVER happen
                else {
                    Toast.makeText(
                        applicationContext,
                        "Employee could not be deleted",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
            //cancel delete
            builder.setNegativeButton("Cancel") { _, _ ->
                Toast.makeText(applicationContext, "Cancelled Deletion", Toast.LENGTH_SHORT).show()
            }

            //start alert dialog
            val alertDialog: AlertDialog = builder.create()
            alertDialog.show()
        }

        // Drop down Spinners for days
        monSpinner = findViewById(R.id.monSpinner)
        ArrayAdapter.createFromResource(
            this,
            R.array.avail_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            monSpinner.adapter = adapter
        }

        tueSpinner = findViewById(R.id.tueSpinner)
        ArrayAdapter.createFromResource(
            this,
            R.array.avail_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            tueSpinner.adapter = adapter
        }

        wedSpinner = findViewById(R.id.wedSpinner)
        ArrayAdapter.createFromResource(
            this,
            R.array.avail_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            wedSpinner.adapter = adapter
        }

        thuSpinner = findViewById(R.id.thuSpinner)
        ArrayAdapter.createFromResource(
            this,
            R.array.avail_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            thuSpinner.adapter = adapter
        }

        friSpinner = findViewById(R.id.friSpinner)
        ArrayAdapter.createFromResource(
            this,
            R.array.avail_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            friSpinner.adapter = adapter
        }

        sunSpinner = findViewById(R.id.sunSpinner)
        ArrayAdapter.createFromResource(
            this,
            R.array.weekend_avail_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            sunSpinner.adapter = adapter
        }

        satSpinner = findViewById(R.id.satSpinner)
        ArrayAdapter.createFromResource(
            this,
            R.array.weekend_avail_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            satSpinner.adapter = adapter
        }
        // End of spinners

        // disable the save button till a change has been made
        editBtn.isEnabled = false

        val curEmployee = dbHelper.findEmpById(curEmpId)

        //store the "old" employee info, will be used in detecting when to enable the save button
        oldFirstName = curEmployee?.firstName ?: ""
        oldLastName = curEmployee?.lastName ?: ""
        oldNickname = curEmployee?.nickName ?: ""
        oldEmail = curEmployee?.email ?: ""
        oldPhoneNum = curEmployee?.phoneNum ?: ""
        oldCanOpen = curEmployee?.canOpen ?: false
        oldCanClose = curEmployee?.canClose ?: false

        // This imports information of Employee into the fields
        binding.firstNameEditText.setText(curEmployee?.firstName ?: "")
        binding.lastNameEditText.setText(curEmployee?.lastName ?: "")
        binding.nickNameEditText.setText(curEmployee?.nickName ?: "")
        binding.emailEditText.setText(curEmployee?.email ?: "")
        binding.phoneNumEditText.setText(curEmployee?.phoneNum ?: "")
        binding.canOpenCheckBox.isChecked = curEmployee?.canOpen ?: false
        binding.cacCloseCheckBox.isChecked = curEmployee?.canClose ?: false

        // this makes the phone number automatically get formatted like (204) 698-4178
        binding.phoneNumEditText.addTextChangedListener(PhoneNumberFormattingTextWatcher())
        // start validations, the button will (hopefully) only be enabled when the form is valid
        // and the user has made a change
        firstNameEditTextListener()
        lastNameEditTextListener()
        emailEditTextListener()
        phoneNumEditTextListener()
        nickNameEditTextListener()
        binding.canOpenCheckBox.setOnCheckedChangeListener { _, _ ->
            enableButtonIfValid()
        }
        binding.cacCloseCheckBox.setOnCheckedChangeListener { _, _ ->
            enableButtonIfValid()
        }

        // Spinner Updates and Validations
        val empAvails = dbHelper.findAvailabilityById(curEmployee!!.empId)
        monSpinner.setSelection(getAvailInt(empAvails, "mon"))
        oldMonSpinner = monSpinner.selectedItem.toString()
        tueSpinner.setSelection(getAvailInt(empAvails, "tue"))
        oldTueSpinner = tueSpinner.selectedItem.toString()
        wedSpinner.setSelection(getAvailInt(empAvails, "wed"))
        oldWedSpinner = wedSpinner.selectedItem.toString()
        thuSpinner.setSelection(getAvailInt(empAvails, "thu"))
        oldThuSpinner = thuSpinner.selectedItem.toString()
        friSpinner.setSelection(getAvailInt(empAvails, "fri"))
        oldFriSpinner = friSpinner.selectedItem.toString()
        satSpinner.setSelection(getAvailInt(empAvails, "sat"))
        oldSatSpinner = satSpinner.selectedItem.toString()
        sunSpinner.setSelection(getAvailInt(empAvails, "sun"))
        oldSunSpinner = sunSpinner.selectedItem.toString()
        spinnerEditListener(monSpinner)
        spinnerEditListener(tueSpinner)
        spinnerEditListener(wedSpinner)
        spinnerEditListener(thuSpinner)
        spinnerEditListener(friSpinner)
        spinnerEditListener(satSpinner)
        spinnerEditListener(sunSpinner)

        editBtn.setOnClickListener {
            if (editWasSuccessful()) {
                Toast.makeText(this, "EMPLOYEE UPDATED SUCCESSFULLY", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "EMPLOYEE COULD NOT BE UPDATED", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun editWasSuccessful(): Boolean {
        val employee = Employee(
            curEmpId,
            binding.firstNameEditText.text.toString(),
            binding.lastNameEditText.text.toString(),
            binding.nickNameEditText.text.toString(),
            binding.emailEditText.text.toString(),
            binding.phoneNumEditText.text.toString(),
            binding.canOpenCheckBox.isChecked,
            binding.cacCloseCheckBox.isChecked,
            true
        )

        val oldEmpAvails = dbHelper.findAvailabilityById(curEmpId)
        val empAvails = EmpAvailability(
            oldEmpAvails!!.availId,
            curEmpId,
            getAvailBool(monSpinner, "AM"),
            getAvailBool(monSpinner, "PM"),
            getAvailBool(tueSpinner, "AM"),
            getAvailBool(tueSpinner, "PM"),
            getAvailBool(wedSpinner, "AM"),
            getAvailBool(wedSpinner, "PM"),
            getAvailBool(thuSpinner, "AM"),
            getAvailBool(thuSpinner, "PM"),
            getAvailBool(friSpinner, "AM"),
            getAvailBool(friSpinner, "PM"),
            getAvailBool(satSpinner, "Full"),
            getAvailBool(sunSpinner, "Full")
        )
        dbHelper.updateAvailability(empAvails)
        return dbHelper.updateEmployee(employee) == 1
    }

    /**
     * This method enables the save button if all the form data is good AND the user has made a change
     */
    private fun enableButtonIfValid() {
        // The form is valid if the validate methods returns null
        val formIsValid = validateName("First") == null &&
                validateName("Last") == null &&
                validateNickname() == null &&
                validateEmail() == null &&
                validatePhoneNum() == null

        // The user has made a change if the current data is different from the old data
        val userHasMadeAChange = binding.firstNameEditText.text.toString() != oldFirstName ||
                binding.lastNameEditText.text.toString() != oldLastName ||
                binding.nickNameEditText.text.toString() != oldNickname ||
                binding.emailEditText.text.toString() != oldEmail ||
                binding.phoneNumEditText.text.toString() != oldPhoneNum ||
                binding.canOpenCheckBox.isChecked != oldCanOpen ||
                binding.cacCloseCheckBox.isChecked != oldCanClose ||
                binding.monSpinner.selectedItem.toString() != oldMonSpinner ||
                binding.tueSpinner.selectedItem.toString() != oldTueSpinner ||
                binding.wedSpinner.selectedItem.toString() != oldWedSpinner ||
                binding.thuSpinner.selectedItem.toString() != oldThuSpinner ||
                binding.friSpinner.selectedItem.toString() != oldFriSpinner ||
                binding.satSpinner.selectedItem.toString() != oldSatSpinner ||
                binding.sunSpinner.selectedItem.toString() != oldSunSpinner

        editBtn.isEnabled = formIsValid && userHasMadeAChange
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
        if (dbHelper.emailExists(email) && email != oldEmail) {
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
        if (dbHelper.phoneNumExists(phoneNum) && phoneNum != oldPhoneNum) {
            return "Phone # already exists"
        }
        return null
    }

    private fun getAvailInt(empAvails: EmpAvailability?, day: String): Int {
        var availInt = 0
        if (day == "mon") {
            if (empAvails!!.monAM && empAvails.monPM) availInt = 3
            else if (empAvails.monAM) availInt = 1
            else if (empAvails.monPM) availInt = 2
            return availInt
        }
        if (day == "tue") {
            if (empAvails!!.tueAM && empAvails.tuePM) availInt = 3
            else if (empAvails.tueAM) availInt = 1
            else if (empAvails.tuePM) availInt = 2
            return availInt
        }
        if (day == "wed") {
            if (empAvails!!.wedAM && empAvails.wedPM) availInt = 3
            else if (empAvails.wedAM) availInt = 1
            else if (empAvails.wedPM) availInt = 2
            return availInt
        }
        if (day == "thu") {
            if (empAvails!!.thuAM && empAvails.thuPM) availInt = 3
            else if (empAvails.thuAM) availInt = 1
            else if (empAvails.thuPM) availInt = 2
            return availInt
        }
        if (day == "fri") {
            if (empAvails!!.friAM && empAvails.friPM) availInt = 3
            else if (empAvails.friAM) availInt = 1
            else if (empAvails.friPM) availInt = 2
            return availInt
        }
        if (day == "sat") {
            if (empAvails!!.sat) availInt = 1
            return availInt
        }
        if (day == "sun") {
            if (empAvails!!.sun) availInt = 1
            return availInt
        }
        return -1
    }

    private fun getAvailBool(spinner: Spinner, time: String): Boolean {
        val spinnerInt = spinner.selectedItemPosition
        if (time == "AM") {
            return spinnerInt == 3 || spinnerInt == 1
        }
        if (time == "PM") {
            return spinnerInt == 3 || spinnerInt == 2
        }
        if (time == "Full") {
            return spinnerInt == 1
        }
        return false
    }


    private fun spinnerEditListener(spinner: Spinner) {
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                enableButtonIfValid()
            }
        }
    }

}