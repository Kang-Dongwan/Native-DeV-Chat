package com.kbds.nativedev.kbchat

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.kbds.nativedev.kbchat.adapters.ListAdapter
import com.kbds.nativedev.kbchat.fragment.ChatFragment
import com.kbds.nativedev.kbchat.fragment.TestData
import kotlinx.android.synthetic.main.activity_profile_detail.*
import kotlinx.android.synthetic.main.fragment_friend.*

class ProfileDetailActivity : AppCompatActivity() {

    private lateinit var deleteFriendBtn: Button
    private lateinit var chatBtn: Button
    private lateinit var chatFragmet: ChatFragment
    val database = Firebase.database
    val myRef = database.getReference("friend")
    val databaseIns = FirebaseDatabase.getInstance().reference
    val auth = Firebase.auth
    val user = auth.currentUser


    private lateinit var listAdapter: ListAdapter
    var dataList: ArrayList<TestData> = arrayListOf(
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_detail)
        dataList.clear();
        var list = dataList
        var imageView = R.id.profileImage
        deleteFriendBtn = findViewById(R.id.deleteFriendBtn)
        chatBtn = findViewById(R.id.chatBtn)


        val name = intent.getStringExtra("name")
        val comment = intent.getStringExtra("comment")
        val friendUid = intent.getStringExtra("friendUid")
        val profileImageUrl = intent.getStringExtra("profileImageUrl")

        textView1.text = "$name"
        textView2.text = "$comment"
        //textView3.text = "ProfileImageUrl: $profileImageUrl"
        //textView4.text = "FriendUid: $friendUid"
        Glide.with(this).load("$profileImageUrl")
            .error(R.drawable.user) // 이미지로드 실패시 로컬 user.png
            .circleCrop()
            .into(profileImage)

        deleteFriendBtn.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("친구삭제")
                .setMessage("현재 친구를 삭제하시겠습니까?")
                .setPositiveButton("확인",
                    DialogInterface.OnClickListener{ dialog, id ->
                        FirebaseDatabase.getInstance().reference.child("user").addValueEventListener(object :
                            ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                for (snapshot in dataSnapshot.children) {
                                    var friendUid = snapshot.key
                                    var email = snapshot.child("email")
                                    var comment = snapshot.child("comment")
                                    var freindUid02 = snapshot.child("uid")

                                    Log.d("test", "test000 = "+ name)   //db
                                    //Log.d("test", "test111 = "+ userName) //input
                                    Log.d("test", "test222 = "+ freindUid02) //input
                                    if(email.value?.equals(name) == true) {
                                        Log.d("test", "test123")
                                        user?.let {
                                            Log.d("FirstFragment", "deleteBtn:userUid:" + user.uid)
                                            myRef.child(user.uid).child(friendUid.toString()).removeValue()
                                            Toast.makeText(this@ProfileDetailActivity, "친구삭제 성공", Toast.LENGTH_LONG).show()
                                            val nextIntent = Intent(this@ProfileDetailActivity, MainActivity::class.java)
                                            startActivity(nextIntent)
                                            return
                                        }
                                    }

                                    //val map = snapshot.getValue(Map::class.java) as Map<String, String>
                                    //val comment = map.get("comment").toString()
                                    //val name = map.get("name").toString()
                                    Log.d("FirstFragment", "ValueEventListener : " + snapshot.value + " a = " + name + "a.value = "+ comment)
                                }
                                Toast.makeText(this@ProfileDetailActivity, "친구삭제 실패", Toast.LENGTH_LONG).show()
                            }

                            override fun onCancelled(databaseError: DatabaseError) {}
                        })
                        //Toast.makeText(this, "친구삭제가 완료돠었습니다.", Toast.LENGTH_SHORT)
                        //    .show()
                    })
                .setNegativeButton("취소",
                    DialogInterface.OnClickListener{ dialog, id ->
                        Toast.makeText(this, "친구삭제에 실패하였습니다.", Toast.LENGTH_SHORT)
                            .show()
                    })
            builder.show()
        }
        chatBtn.setOnClickListener {

            val mFragmentManager = supportFragmentManager

            val mFragmentTransaction = mFragmentManager.beginTransaction()
            val mFragment = ChatFragment()

            // On button click, a bundle is initialized and the
            // text from the EditText is passed in the custom
            // fragment using this bundle\
             /*   val mBundle = Bundle()
                mBundle.putString("friendUid",friendUid.toString())
                mFragment.arguments = mBundle
                mFragmentTransaction.add(R.id.container, mFragment).commit()*/
            //finish()
            val nextIntent = Intent(this, MainActivity::class.java)
            nextIntent.putExtra("friendUid", friendUid)
            startActivity(nextIntent)

            val bundle = Bundle()
            bundle.putString("friendUid", friendUid)
            val myFrag = ChatFragment()
            //myFrag.setArguments(bundle)
            //mFragmentTransaction.replace(R.id.fragment_frame, myFrag).commit()

            //chatFragmet = ChatFragment.newInstance()
            //chatFragmet.setArguments(bundle)
            //supportFragmentManager.beginTransaction().replace(R.id.container, chatFragmet).commit()
        }
    }

}