package com.jpmc.jpmcscheduler

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_main.*
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FirebaseAuth
import android.support.annotation.NonNull
import android.app.ProgressDialog
import android.content.Context
import android.transition.TransitionManager
import android.widget.SpinnerAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.generate_token.*
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    var select_slot = 0

    lateinit var auth: FirebaseAuth
    lateinit var user: FirebaseUser
    lateinit var PD: ProgressDialog
    lateinit var con: Context
    lateinit var timeSlots: MutableList<String>
    lateinit var dialog1: ProgressDialog
    var arriveT = 0
    var BankAct = 0
    var BankActivityList: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        if(auth.currentUser==null){
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            finish()
        }

        PD = ProgressDialog(this)
        PD.setMessage("Loading...")
        PD.setCancelable(true)
        PD.setCanceledOnTouchOutside(false)

        dialog1 = ProgressDialog(this)
        dialog1.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        dialog1.setMessage("Loading. Please wait...")
        dialog1.setIndeterminate(true)
        dialog1.setCanceledOnTouchOutside(false)



        sign_out_button.setOnClickListener(View.OnClickListener {
            auth.signOut()
            val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                val user = firebaseAuth.currentUser
                if (user == null) {
                    startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                    finish()
                }
            }
        })

        bottom_navigation.setOnNavigationItemSelectedListener{
            TransitionManager.beginDelayedTransition(main_activity)
            var check = false
            if(it.itemId == R.id.action_add_token){
                generate_token_action.visibility = View.VISIBLE
                view_token_action.visibility  = View.GONE
                chatbot_action.visibility = View.GONE
                check = true
            }
            if(it.itemId == R.id.action_view_token){
                generate_token_action.visibility = View.GONE
                view_token_action.visibility  = View.VISIBLE
                chatbot_action.visibility = View.GONE
                populate_user_tokens()
                check = true
            }
            if(it.itemId == R.id.action_chatbot){
                generate_token_action.visibility = View.GONE
                view_token_action.visibility  = View.GONE
                chatbot_action.visibility = View.VISIBLE
                check = true
            }
            check
        }




        BankActivityList.add("Select the bank activity")
        BankActivityList.add("Savings")
        BankActivityList.add("Withdrawal")
        BankActivityList.add("Cheques")
        BankActivityList.add("Create Account")

        bankActivity.adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                ArrayList(BankActivityList))

        bankActivity.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
                BankAct = 0
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                BankAct = position
            }
        }



        next.setOnClickListener {
            var count = 0
            if (arrivalTime.text.isBlank()) {
                count++;
                var temp = AlertDialog.Builder(this)
                temp.setTitle("Enter valid time")
                temp.setPositiveButton("Okay", { dialogInterface: DialogInterface, i: Int -> })
                temp.show()
            }

            if(BankAct == 0)
            {
                count++
                var temp = AlertDialog.Builder(this)
                temp.setTitle("Enter valid Bank Activity")
                temp.setPositiveButton("Okay", { dialogInterface: DialogInterface, i: Int -> })
                temp.show()
            }

            if(count==0){
                arriveT = arrivalTime.text.toString().toInt()
                var tokenIntent = Intent(this,TokenGenerator::class.java)
                timeslot.visibility = View.VISIBLE
                gen_token.visibility = View.VISIBLE
                dialog1.show()
                populate_timeslots()
            }
        }

        var databaseref = FirebaseDatabase.getInstance().getReference()

        val cal = Calendar.getInstance()
        cal.time = Date()
        val df = SimpleDateFormat("MMMM d, yyyy")
        var dat = df.format(cal.time)

        databaseref = databaseref.child(dat).child(BankActivityList[BankAct])

        timeSlots = mutableListOf()

        con = this

        databaseref.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {
                if(p0!!.exists()){
                    timeSlots.clear()
                    timeSlots.add("Select the Time Slot")
                    for(p in p0.children){
                        var token = p.getValue(String::class.java)
                        if(token.equals("0")){
                            var temp = p.key
                            var time = Date()
                            time.hours = temp?.substring(0,2)!!.toInt()
                            time.minutes = temp?.substring(3,5)!!.toInt()
                            time.seconds = 0
                            val cal = Calendar.getInstance()
                            cal.time = Date()
                            cal.add(Calendar.MINUTE, arriveT)

                            if(time.compareTo(cal.time)>0){
                                timeSlots.add(temp)
                            }
                        }
                    }
                    timeslot.adapter = ArrayAdapter<String>(con, android.R.layout.simple_list_item_1,
                            ArrayList(timeSlots))
                    dialog1.dismiss()
                }
            }

        })

        timeslot.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
                select_slot = 0
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                select_slot = position
            }

        }



        gen_token.setOnClickListener {
            var count = 0
            if(select_slot == 0){
                count++
                var temp = AlertDialog.Builder(this)
                temp.setTitle("Enter valid time")
                temp.setPositiveButton("Okay", { dialogInterface: DialogInterface, i: Int -> })
                temp.show()
            }

            if(count==0){
                transactionOp()
            }
        }
    }

    private fun populate_user_tokens() {
        var databaseref = FirebaseDatabase.getInstance().getReference()
    }


    private fun populate_timeslots() {
        var databaseref = FirebaseDatabase.getInstance().getReference()

        val cal = Calendar.getInstance()
        cal.time = Date()
        val df = SimpleDateFormat("MMMM d, yyyy")
        var dat = df.format(cal.time)

        databaseref = databaseref.child(dat).child(BankActivityList[BankAct])

        timeSlots = mutableListOf()

        con = this

        databaseref.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {
                if(p0!!.exists()){
                    timeSlots.clear()
                    timeSlots.add("Select the Time Slot")
                    for(p in p0.children){
                        var token = p.getValue(String::class.java)
                        if(token.equals("0")){
                            var temp = p.key
                            var time = Date()
                            time.hours = temp?.substring(0,2)!!.toInt()
                            time.minutes = temp?.substring(3,5)!!.toInt()
                            time.seconds = 0
                            val cal = Calendar.getInstance()
                            cal.time = Date()
                            cal.add(Calendar.MINUTE, arriveT)

                            if(time.compareTo(cal.time)>0){
                                timeSlots.add(temp)
                            }
                        }
                    }
                    timeslot.adapter = ArrayAdapter<String>(con, android.R.layout.simple_list_item_1,
                            ArrayList(timeSlots))
                    dialog1.dismiss()
                }
            }

        })
    }

    private fun transactionOp() {
        var databaseref = FirebaseDatabase.getInstance().getReference()

        var User = FirebaseAuth.getInstance().currentUser
        var utot = FirebaseDatabase.getInstance().getReference()
        var count = 0L
        var entry = true

        val cal = Calendar.getInstance()
        cal.time = Date()
        val df = SimpleDateFormat("MMMM d, yyyy")
        var dat = df.format(cal.time)

        utot.child(dat).child("Tokens").child("count").addValueEventListener(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {
                if(p0!!.exists()) {
                    count = p0.value as Long

                    if (entry == true) {
                        entry = false
                        databaseref = databaseref.child(dat).child(BankActivityList[BankAct])//.child(timeSlots[select_slot])

                        var key = databaseref.child(timeSlots[select_slot]).push().key

                        var c1 = false
                        var c2 = false

                        databaseref.child(timeSlots[select_slot]).setValue(key).addOnSuccessListener {
                            c1 = true
                        }




                        count++

                        utot.child(dat).child("Tokens").child("count").setValue(count).addOnSuccessListener {
                            c2 = true
                        }

                        utot.child("Users").child(User?.email?.replace(".", "")).child("Tokens")
                                .child(key).setValue(
                                        Token(
                                                date = dat,
                                                timeslot = timeSlots[select_slot],
                                                token = key,
                                                ba = BankActivityList[BankAct],
                                                number = count
                                        )
                                ).addOnSuccessListener {
                                    if (c1 and c2) {
                                        var temp = AlertDialog.Builder(con)
                                        temp.setTitle("Slot Booked!")
                                        temp.setPositiveButton("Okay", { dialogInterface: DialogInterface, i: Int ->
                                            view_token_action.callOnClick()
                                        })
                                        temp.show()
                                    }
                                }
                    }
                }
            }

        })
    }

}
