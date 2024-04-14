package com.example.hopperrestaurant

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.contentValuesOf
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

const val DB_NAME = "HopperRestaurantDb"

//EMPLOYEE TABLE NAME, FIELDS AND KEYS
const val EMP_TABLE_NAME = "Employees"
const val EMP_TABLE_ID = "empId"
const val EMP_TABLE_FNAME = "fName"
const val EMP_TABLE_LNAME = "lName"
const val EMP_TABLE_EMAIL = "email"
const val EMP_TABLE_PHONE_NUM = "phoneNum"
const val EMP_TABLE_CAN_OPEN = "canOpen"
const val EMP_TABLE_CAN_CLOSE = "canClose"
const val EMP_TABLE_STATUS = "status"
const val EMP_TABLE_NNAME = "nName"

//EMPLOYEE AVAILABILITIES TABLE NAME, FIELDS AND KEYS
const val EMP_AVAILABILITY = "Emp_avail"
const val A_ID = "Avail_id"
const val MON_AM = "MonAm"
const val MON_PM = "MonPm"
const val TUE_AM = "TueAm"
const val TUE_PM = "TuePm"
const val WED_AM = "WedAm"
const val WED_PM = "WedPm"
const val THU_AM = "ThuAm"
const val THU_PM = "ThuPm"
const val FRI_AM = "FriAm"
const val FRI_PM = "FriPm"
const val SAT = "Sat"
const val SUN = "Sun"


//MONTHLY SCHEDULE TABLE NAME, FIELDS AND KEYS
const val MONTH_SCHED = "schedule"
const val S_ID = "sId"
const val DATE = "date"
const val TIME = "amOrPm"
const val IS_BUSY_DAY = "isBusyDay"

//**************************************************************************************************
class DBHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, 1) {
    // Is executed when the database does not exist
    override fun onCreate(db: SQLiteDatabase?) {

        //EMPLOYEE INFORMATION TABLE
        val createTable =
            """
                CREATE TABLE $EMP_TABLE_NAME
                (
                    $EMP_TABLE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $EMP_TABLE_NNAME TEXT(20),
                    $EMP_TABLE_FNAME TEXT(20) NOT NULL,
                    $EMP_TABLE_LNAME TEXT(20) NOT NULL,
                    $EMP_TABLE_EMAIL TEXT(40) NOT NULL UNIQUE,
                    $EMP_TABLE_PHONE_NUM TEXT(12) NOT NULL UNIQUE,
                    $EMP_TABLE_CAN_OPEN INTEGER NOT NULL,
                    $EMP_TABLE_CAN_CLOSE INTEGER NOT NULL,
                    $EMP_TABLE_STATUS INTEGER NOT NULL
                )
            """.trimIndent()

        db?.execSQL(createTable)

        //EMPLOYEE AVAILABILITIES TABLE
        val createTable2 =
            """
                CREATE TABLE $EMP_AVAILABILITY 
                (
                    $A_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $EMP_TABLE_ID INTEGER NOT NULL,
                    $MON_AM INTEGER NOT NULL,
                    $MON_PM INTEGER NOT NULL,
                    $TUE_AM INTEGER NOT NULL,
                    $TUE_PM INTEGER NOT NULL,
                    $WED_AM INTEGER NOT NULL,
                    $WED_PM INTEGER NOT NULL,
                    $THU_AM INTEGER NOT NULL,
                    $THU_PM INTEGER NOT NULL,
                    $FRI_AM INTEGER NOT NULL,
                    $FRI_PM INTEGER NOT NULL,
                    $SAT INTEGER NOT NULL,
                    $SUN INTEGER NOT NULL,
                    FOREIGN KEY ($EMP_TABLE_ID) REFERENCES $EMP_TABLE_NAME
                    );

            """.trimIndent()

        db?.execSQL(createTable2)

        //MONTHLY SCHEDULE TABLE
        val createTable3 =
            """
       CREATE TABLE $MONTH_SCHED 
       (
           $S_ID INTEGER PRIMARY KEY AUTOINCREMENT,
           $DATE TEXT NOT NULL,
           $TIME TEXT NOT NULL,
           $EMP_TABLE_ID INTEGER NOT NULL,
           $IS_BUSY_DAY INTEGER NOT NULL,
           FOREIGN KEY ($EMP_TABLE_ID) REFERENCES $EMP_TABLE_NAME
       );
    """.trimIndent()
        db?.execSQL(createTable3)

    }
//**************************************************************************************************

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }

    //CHECK IF PHONE NUMBER EXISTS IN DB
    fun phoneNumExists(phoneNum: String): Boolean {
        val query =
            "SELECT $EMP_TABLE_PHONE_NUM FROM $EMP_TABLE_NAME WHERE $EMP_TABLE_PHONE_NUM = '$phoneNum' LIMIT 1"

        val db = this.readableDatabase
        val result = db.rawQuery(query, null)

        // returns true if the query returned a data, false if not
        val pNumExists = result.moveToNext()

        result.close()
        db.close()
        return pNumExists
    }

    //CHECK IF EMAIL EXIST IN DB
    fun emailExists(email: String): Boolean {
        val query =
            "SELECT $EMP_TABLE_EMAIL FROM $EMP_TABLE_NAME WHERE $EMP_TABLE_EMAIL = '$email' LIMIT 1"

        val db = this.readableDatabase
        val result = db.rawQuery(query, null)

        // returns true if the query returned a data, false if not
        val emailExists = result.moveToNext()

        result.close()
        db.close()
        return emailExists
    }

    //DEACTIVATE AND EMPLOYEE FROM THE DB
    fun deactivateEmployee(empId: Long): Int {
        val db = this.writableDatabase
        val dataToUpdate = contentValuesOf(
            Pair(EMP_TABLE_STATUS, false)
        )
        val numEmpDeleted =
            db.update(EMP_TABLE_NAME, dataToUpdate, "$EMP_TABLE_ID = ?", arrayOf(empId.toString()))
        // employee has been deactivated, now delete all their schedules
        if (numEmpDeleted > 0) {
            db.delete(MONTH_SCHED, "$EMP_TABLE_ID = ?", arrayOf(empId.toString()))
        }
        db.close()
        return numEmpDeleted
    }

    //ADD AND EMPLOYEE TO THE DB
    fun addEmployee(employee: Employee): Long {
        val data = contentValuesOf(
            Pair(EMP_TABLE_FNAME, employee.firstName),
            Pair(EMP_TABLE_LNAME, employee.lastName),
            Pair(EMP_TABLE_NNAME, employee.nickName),
            Pair(EMP_TABLE_EMAIL, employee.email.lowercase()),
            Pair(EMP_TABLE_PHONE_NUM, employee.phoneNum),
            Pair(EMP_TABLE_CAN_OPEN, employee.canOpen),
            Pair(EMP_TABLE_CAN_CLOSE, employee.canClose),
            Pair(EMP_TABLE_STATUS, employee.status),
        )

        val db = this.writableDatabase
        val empId = db.insert(EMP_TABLE_NAME, null, data)
        db.close()
        return empId
    }

    //UPDATE EMPLOYEE INFO IN THE DB
    /**
     * To whoever calling this function, if the returnValue is not equal to 1, something went wrong
     * If the return value is 0, the employee does not exist. You should check if the employee exists
     * before calling this function
     *
     * If the return value is > 1, that means more than 1 employee was updated. This should not happen
     * under any circumstances
     * */
    fun updateEmployee(employee: Employee): Int {
        val data = contentValuesOf(
            Pair(EMP_TABLE_FNAME, employee.firstName),
            Pair(EMP_TABLE_LNAME, employee.lastName),
            Pair(EMP_TABLE_NNAME, employee.nickName),
            Pair(EMP_TABLE_EMAIL, employee.email.lowercase()),
            Pair(EMP_TABLE_PHONE_NUM, employee.phoneNum),
            Pair(EMP_TABLE_CAN_OPEN, employee.canOpen),
            Pair(EMP_TABLE_CAN_CLOSE, employee.canClose),
            Pair(EMP_TABLE_STATUS, employee.status),
        )

        val db = this.writableDatabase
        val rowsAffected =
            db.update(EMP_TABLE_NAME, data, "$EMP_TABLE_ID = ?", arrayOf(employee.empId.toString()))
        db.close()
        return rowsAffected
    }

    fun updateAvailability(avail: EmpAvailability): Int {
        val data = contentValuesOf(
            Pair(MON_AM, avail.monAM),
            Pair(MON_PM, avail.monPM),
            Pair(TUE_AM, avail.tueAM),
            Pair(TUE_PM, avail.tuePM),
            Pair(WED_AM, avail.wedAM),
            Pair(WED_PM, avail.wedPM),
            Pair(THU_AM, avail.thuAM),
            Pair(THU_PM, avail.thuPM),
            Pair(FRI_AM, avail.friAM),
            Pair(FRI_PM, avail.friPM),
            Pair(SAT, avail.sat),
            Pair(SUN, avail.sun),
        )

        val db = this.writableDatabase
        val rowsAffected =
            db.update(EMP_AVAILABILITY, data, "$A_ID = ?", arrayOf(avail.availId.toString()))
        db.close()
        return rowsAffected
    }


    //GET LIST OF ALL EMPLOYEES IN DB
    fun getAllEmployees(): ArrayList<Employee> {
        val employees: ArrayList<Employee> = ArrayList()

        val query = "SELECT * FROM $EMP_TABLE_NAME WHERE $EMP_TABLE_STATUS = true"
        val db = this.readableDatabase
        val result = db.rawQuery(query, null)

        if (result.moveToFirst()) {
            do {
                val employee = Employee(
                    result.getLong(getColumnIndex(result, EMP_TABLE_ID)),
                    result.getString(getColumnIndex(result, EMP_TABLE_FNAME)),
                    result.getString(getColumnIndex(result, EMP_TABLE_LNAME)),
                    result.getString(getColumnIndex(result, EMP_TABLE_NNAME)),
                    result.getString(getColumnIndex(result, EMP_TABLE_EMAIL)),
                    result.getString(getColumnIndex(result, EMP_TABLE_PHONE_NUM)),
                    result.getInt(getColumnIndex(result, EMP_TABLE_CAN_OPEN)) == 1,
                    result.getInt(getColumnIndex(result, EMP_TABLE_CAN_CLOSE)) == 1,
                    result.getInt(getColumnIndex(result, EMP_TABLE_STATUS)) == 1,
                )
                employees.add(employee)
            } while (result.moveToNext())
        }
        result.close()
        db.close()
        return employees
    }


    // This method gets the index of a column in the database
    private fun getColumnIndex(result: Cursor, colName: String): Int {
        return result.getColumnIndex(colName)
    }

    //DELETE ALL EMPLOYEES FROM DB --> **ONLY FOR TESTING**
    fun deleteAllEmployees() {
        val db = this.writableDatabase
        db.delete(EMP_TABLE_NAME, null, null)
        db.close()
    }

    //FIND AN EMPLOYEE BY ID
    fun findEmpById(empId: Long): Employee? {
        val query = "SELECT * FROM $EMP_TABLE_NAME WHERE $EMP_TABLE_ID = $empId"
        val db = this.readableDatabase
        val result = db.rawQuery(query, null)
        var employee: Employee? = null

        if (result.moveToFirst()) {
            employee = Employee(
                result.getLong(getColumnIndex(result, EMP_TABLE_ID)),
                result.getString(getColumnIndex(result, EMP_TABLE_FNAME)),
                result.getString(getColumnIndex(result, EMP_TABLE_LNAME)),
                result.getString(getColumnIndex(result, EMP_TABLE_NNAME)),
                result.getString(getColumnIndex(result, EMP_TABLE_EMAIL)),
                result.getString(getColumnIndex(result, EMP_TABLE_PHONE_NUM)),
                result.getInt(getColumnIndex(result, EMP_TABLE_CAN_OPEN)) == 1,
                result.getInt(getColumnIndex(result, EMP_TABLE_CAN_CLOSE)) == 1,
                result.getInt(getColumnIndex(result, EMP_TABLE_STATUS)) == 1,
            )
        }
        result.close()
        db.close()
        return employee

    }

    //check availability in availability table given day of week
    //add availabilities to availability table
    //add shift to monthly schedule table
    fun addAvailability(availability: EmpAvailability): Long {
        val data = contentValuesOf(
            Pair(EMP_TABLE_ID, availability.empId),
            Pair(MON_AM, availability.monAM),
            Pair(MON_PM, availability.monPM),
            Pair(TUE_AM, availability.tueAM),
            Pair(TUE_PM, availability.tuePM),
            Pair(WED_AM, availability.wedAM),
            Pair(WED_PM, availability.wedPM),
            Pair(THU_AM, availability.thuAM),
            Pair(THU_PM, availability.thuPM),
            Pair(FRI_AM, availability.friAM),
            Pair(FRI_PM, availability.friPM),
            Pair(SAT, availability.sat),
            Pair(SUN, availability.sun),
        )

        val db = this.writableDatabase
        val availId = db.insert(EMP_AVAILABILITY, null, data)
        db.close()
        return availId
    }

    fun findAvailabilityById(empId: Long): EmpAvailability? {
        val query = "SELECT * FROM $EMP_AVAILABILITY WHERE $EMP_TABLE_ID = $empId"
        val db = this.readableDatabase
        val result = db.rawQuery(query, null)
        var avail: EmpAvailability? = null

        if (result.moveToFirst()) {
            avail = EmpAvailability(
                result.getLong(getColumnIndex(result, A_ID)),
                result.getLong(getColumnIndex(result, EMP_TABLE_ID)),
                result.getInt(getColumnIndex(result, MON_AM)) == 1,
                result.getInt(getColumnIndex(result, MON_PM)) == 1,
                result.getInt(getColumnIndex(result, TUE_AM)) == 1,
                result.getInt(getColumnIndex(result, TUE_PM)) == 1,
                result.getInt(getColumnIndex(result, WED_AM)) == 1,
                result.getInt(getColumnIndex(result, WED_PM)) == 1,
                result.getInt(getColumnIndex(result, THU_AM)) == 1,
                result.getInt(getColumnIndex(result, THU_PM)) == 1,
                result.getInt(getColumnIndex(result, FRI_AM)) == 1,
                result.getInt(getColumnIndex(result, FRI_PM)) == 1,
                result.getInt(getColumnIndex(result, SAT)) == 1,
                result.getInt(getColumnIndex(result, SUN)) == 1,
            )
        }
        result.close()
        db.close()
        return avail

    }

    /*Check for duplicate records in monthly schedule table and return true if a duplicate is found
      and false otherwise*/
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("Range")
    fun checkDupShift(date: LocalDate, time: String, empId: Long): Boolean? {
        val dateString = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val query = """
            SELECT COUNT($S_ID) AS result
            FROM $MONTH_SCHED
            WHERE $DATE = '$dateString' AND $TIME = '$time' AND $EMP_TABLE_ID = '$empId';
        """.trimIndent()
        val db = writableDatabase
        val cursor = db.rawQuery(query, null)
        cursor.moveToFirst()
        val result = cursor.getInt(cursor.getColumnIndex("result"))
        cursor.close()
        return if (result == 0) true else if (result >= 1) false else null
    }

    //Update the monthly schedule table
    fun updateShift(monSchedule: Schedule): Int {
        val data = contentValuesOf(
            Pair(EMP_TABLE_ID, monSchedule.empid)
        )
        val db = this.writableDatabase
        val rowsAffected =
            db.update(MONTH_SCHED, data, "$EMP_TABLE_ID = ?", arrayOf(monSchedule.empid.toString()))
        db.close()
        return rowsAffected
    }

    fun addShift(monSchedule: Schedule): Long {
        val data = contentValuesOf(
            Pair(DATE, monSchedule.date),
            Pair(TIME, monSchedule.time),
            Pair(EMP_TABLE_ID, monSchedule.empid),
            Pair(IS_BUSY_DAY, monSchedule.isBusyDay)
        )
        val db = this.writableDatabase
        val shiftId = db.insert(MONTH_SCHED, null, data)
        db.close()
        return shiftId
    }

    /*
    * Returns true if the employee can work both AM and PM shifts on the given day,
    * false if not
    * */
    fun canWorkFullShift(empId: Long, dayAndTime: String): Boolean {
        val day = dayAndTime.substring(0, 3) // remove AM/PM
        val dayAm = day + "Am"
        val dayPm = day + "Pm"
        // brief: select emp id from availability where emp id = empId (from argument)
        // and both "dayAm" and "dayPm" are true
        val query = """
            SELECT
                (SELECT $EMP_TABLE_ID FROM $EMP_TABLE_NAME WHERE $EMP_TABLE_NAME.$EMP_TABLE_ID = $EMP_AVAILABILITY.$EMP_TABLE_ID) [$EMP_TABLE_ID]
            FROM 
                $EMP_AVAILABILITY
            WHERE
                $EMP_AVAILABILITY.$EMP_TABLE_ID = $empId AND $EMP_AVAILABILITY.$dayAm = 1 AND $EMP_AVAILABILITY.$dayPm = 1
        """.trimIndent()
        val db = this.readableDatabase
        val result = db.rawQuery(query, null)

        val canWorkFullShift =
            result.moveToFirst() // if there is a result, the employee can work both AM and PM
        result.close()
        db.close()
        return canWorkFullShift
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getScheduledEmployees(date: LocalDate, amOrPm: String): ArrayList<Employee> {
        val dateString = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val time = if (amOrPm == "Am") "Am" else if (amOrPm == "Pm") "Pm" else "Full"
        val employees: ArrayList<Employee> = ArrayList()

        val query = """
            SELECT
                $EMP_TABLE_ID
            FROM 
                $MONTH_SCHED
            WHERE
                $DATE = '$dateString' AND $TIME = '$time'
        """.trimIndent()

        val db = this.readableDatabase
        val result = db.rawQuery(query, null)

        if (result.moveToFirst()) { // got some ids, now get the employee objects and save
            do {
                val empId = result.getLong(getColumnIndex(result, EMP_TABLE_ID))
                val employee = findEmpById(empId)
                if (employee != null) {
                    employees.add(employee)
                }
            } while (result.moveToNext())
        }
        result.close()
        db.close()
        return employees
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getAvailableEmployees(date: LocalDate, amOrPm: String): ArrayList<Employee> {
        // get the first 3 letters of the day, convert to lowercase, then uppercase the first letter
        // and add the AM/PM. After, day will be something like "MonAm" or "TuePm" or Sat
        val day = date.dayOfWeek.toString().substring(0, 3).lowercase()
            .replaceFirstChar { it.uppercase() } + amOrPm
        val employees: ArrayList<Employee> = ArrayList()

        val query = """
            SELECT
                 $EMP_TABLE_NAME.$EMP_TABLE_ID
            FROM 
                $EMP_AVAILABILITY, $EMP_TABLE_NAME
            WHERE
                $EMP_TABLE_NAME.$EMP_TABLE_ID = $EMP_AVAILABILITY.$EMP_TABLE_ID AND  $day = 1 AND $EMP_TABLE_STATUS = 1
        """.trimIndent()

        val db = this.readableDatabase
        val result = db.rawQuery(query, null)

        if (result.moveToFirst()) { // got some ids, now get the employee objects and save
            do {
                val empId = result.getLong(getColumnIndex(result, EMP_TABLE_ID))
                val employee = findEmpById(empId)
                if (employee != null) {
                    employees.add(employee)
                }
            } while (result.moveToNext())
        }
        result.close()
        db.close()
        return employees
    }

    fun deleteAllShifts(date: String) {
        val db = this.writableDatabase
        db.delete(MONTH_SCHED, "$DATE = ?", arrayOf(date))
        db.close()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun isBusyDay(date: LocalDate): Boolean {
        val dateString = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val query = """
            SELECT $IS_BUSY_DAY
            FROM $MONTH_SCHED
            WHERE $DATE = '$dateString' AND $IS_BUSY_DAY = 1
        """.trimIndent()
        val db = this.readableDatabase
        val result = db.rawQuery(query, null)

        val isBusy = result.moveToFirst() // if there is a result, day is busy
        result.close()
        db.close()
        return isBusy
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun isFullyScheduled(date: LocalDate): Boolean {
        val isBusy = isBusyDay(date)
        val isWeekday = date.dayOfWeek != DayOfWeek.SATURDAY && date.dayOfWeek != DayOfWeek.SUNDAY

        /*
        * A shift is fully scheduled if:
        * 1. It's NOT busy:
        *       a. AND it's a weekday. In this case, there must be 2 employees scheduled for both am and pm
        *       b. AND it's a weekend. In this case, there must be 1 employee scheduled for the full day
        * 2. It IS busy:
        *       a. AND it's a weekday. In this case, there must be 3 employees scheduled for both am and pm
        *       b. AND it's a weekend. In this case, there must be 2 employees scheduled for the full day
        * */
        if (!isBusy) {
            if (isWeekday) {// day is not busy, and it's a weekday
                val amEmployees = getScheduledEmployees(date, "Am")
                val pmEmployees = getScheduledEmployees(date, "Pm")
                return amEmployees.size == 2 && pmEmployees.size == 2
            } else { // day is not busy, and it's a weekend
                val fullEmployees = getScheduledEmployees(date, "Full")
                return fullEmployees.size == 2
            }
        }
        else{
            if (isWeekday) {// day is busy, and it's a weekday
                val amEmployees = getScheduledEmployees(date, "Am")
                val pmEmployees = getScheduledEmployees(date, "Pm")
                return amEmployees.size == 3 && pmEmployees.size == 3
            } else { // day is busy, and it's a weekend
                val fullEmployees = getScheduledEmployees(date, "Full")
                return fullEmployees.size == 3
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun isPartiallyScheduled(date : LocalDate) : Boolean {
        val isBusy = isBusyDay(date)
        val isWeekday = date.dayOfWeek != DayOfWeek.SATURDAY && date.dayOfWeek != DayOfWeek.SUNDAY

        /*
        * A shift is partially scheduled if:
        * 1. It's NOT busy:
        *       a. AND it's a weekday. CASE 1: there is 1 employees scheduled for both am and pm
        *       b. OR it's a weekday. CASE 2: there is 1 employees scheduled for am and 2 for pm
        *       c. OR it's a weekday. CASE 3: there is 2 employees scheduled for am and 1 for pm
        *       d. AND it's a weekend. CASE 4: there is 1 employee scheduled for the full day
        * 2. It IS busy:
        *       a. AND it's a weekday. CASE 1: there is 1 or 2 employees scheduled for both am and pm
        *       b. OR it's a weekday. CASE 2: there is 1 or 2 employees scheduled for am and 3 for pm
        *       c. OR it's a weekday. CASE 3: there is 3 employees scheduled for am and 1 or 2 for pm
        *       d. AND it's a weekend. CASE 4: there is 1 or 2 employee scheduled for the full day
        */
        if (!isBusy) {
            if (isWeekday) {// day is not busy, and it's a weekday
                val amEmployees = getScheduledEmployees(date, "Am")
                val pmEmployees = getScheduledEmployees(date, "Pm")
                return amEmployees.size == 1  && pmEmployees.size == 1 ||
                        amEmployees.size == 1  && pmEmployees.size == 2 ||
                        amEmployees.size == 2  && pmEmployees.size == 1 ||
                        amEmployees.size == 1 && pmEmployees.size == 0 ||
                        amEmployees.size == 0 && pmEmployees.size == 1 ||
                        amEmployees.size == 2 && pmEmployees.size == 0 ||
                        amEmployees.size == 0 && pmEmployees.size == 2

            } else { // day is not busy, and it's a weekend
                val fullEmployees = getScheduledEmployees(date, "Full")
                return fullEmployees.size == 1
            }
        }
        else{
            if (isWeekday) {// day is busy, and it's a weekday
                val amEmployees = getScheduledEmployees(date, "Am")
                val pmEmployees = getScheduledEmployees(date, "Pm")
                return amEmployees.size in 1..2 && pmEmployees.size >= 1 && pmEmployees.size < 3 ||
                        amEmployees.size ==  3 && pmEmployees.size >= 1 && pmEmployees.size < 3 ||
                        amEmployees.size in 1..2 && pmEmployees.size == 3 ||
                        amEmployees.size in 1..2 && pmEmployees.size == 0 ||
                        amEmployees.size == 0 && pmEmployees.size >= 1 && pmEmployees.size < 3

            } else { // day is busy, and it's a weekend
                val fullEmployees = getScheduledEmployees(date, "Full")
                return fullEmployees.size in 1..2
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun isNotScheduled(date : LocalDate) : Boolean {
        val isBusy = isBusyDay(date)
        val isWeekday = date.dayOfWeek != DayOfWeek.SATURDAY && date.dayOfWeek != DayOfWeek.SUNDAY
        if (!isBusy) {
            if (isWeekday) {// day is not busy, and it's a weekday
                val amEmployees = getScheduledEmployees(date, "Am")
                val pmEmployees = getScheduledEmployees(date, "Pm")
                return amEmployees.isEmpty() && pmEmployees.isEmpty()
            } else { // day is not busy, and it's a weekend
                val fullEmployees = getScheduledEmployees(date, "Full")
                return fullEmployees.isEmpty()
            }
        } else {
            return false
        }
    }


    //This function retrieves all the records in the MONTH_SCHED, EMP_TABLE_NAME tables and returns
    // the data as a array strings. Takes a substring as the parameter
    fun getMonthlyScheduleData(formattedMonthYear: String): MutableList<Array<String>> {
        val db = this.readableDatabase

        //Query that returns all rows that contain the formattedMonthYear substring
        val query = """
                SELECT  $MONTH_SCHED.$DATE, 
                        $MONTH_SCHED.$TIME, 
                        $EMP_TABLE_NAME.$EMP_TABLE_FNAME,
                        $EMP_TABLE_NAME.$EMP_TABLE_NNAME,
                        $EMP_TABLE_NAME.$EMP_TABLE_LNAME
                        
                FROM    $MONTH_SCHED, $EMP_TABLE_NAME
                
                WHERE   $MONTH_SCHED.$EMP_TABLE_ID = $EMP_TABLE_NAME.$EMP_TABLE_ID 
                
                AND     $MONTH_SCHED.$DATE  LIKE   '$formattedMonthYear%'
                """.trimIndent()
        val cursor = db.rawQuery(query, null)

        //Array container for the row of records
        val data = mutableListOf<Array<String>>()

        //loop through each record and convert it to formatted string
        if (cursor.moveToFirst()) {
            do {
                val date = cursor.getString(0)
                val time = cursor.getString(1)
                val fname = cursor.getString(2)
                val nname = cursor.getString(3)
                val lname = cursor.getString(4)

                val format = arrayOf(date, time, fname, nname, lname)

                data.add(format)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return data
    }
}