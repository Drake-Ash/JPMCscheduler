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



class MainActivity : AppCompatActivity() {

    lateinit var auth: FirebaseAuth
    lateinit var user: FirebaseUser
    lateinit var PD: ProgressDialog
    var BankAct = 0

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


        var BankActivityList: MutableList<String> = mutableListOf()

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
                var arriveT = arrivalTime.text.toString().toInt()
                var tokenIntent = Intent(this,TokenGenerator::class.java)
                tokenIntent.putExtra("BankActivity", BankActivityList[BankAct])
                tokenIntent.putExtra("arrivalTime", arriveT)
                startActivity(tokenIntent)
            }
        }

    }
}
