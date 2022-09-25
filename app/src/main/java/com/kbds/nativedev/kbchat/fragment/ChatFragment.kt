package com.kbds.nativedev.kbchat.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageView
import android.widget.TextView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.kbds.nativedev.kbchat.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.kbds.nativedev.kbchat.MainActivity
import com.kbds.nativedev.kbchat.model.ChatRoom
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.kbds.nativedev.kbchat.ChatActivity
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList
import java.time.format.DateTimeFormatter

// 파이어베이스 접근하기 위한 객체 생성
//private lateinit var database: DatabaseReference
private val database = FirebaseDatabase.getInstance().reference


// chatList 데이터 모델
data class ChatListModel(
    private var uid: String? = null        // 사용자의 uid
    , private var chatId: String? = null     // 사용자가 속한 채팅방id
    , private var chatName: String? = null      // 채팅방의 이름
    , private var lastMessage: String? = null   // 마지막메시지
    , private var chatImageUrl: String? = null  // 채팅방 이미지
    , private var delYn: String? = null         // 채팅방 나감 여부 (Y/N)
    , private var msgCnt: Long? = 0             // 안읽은 메시지 수
    , private var visitYn: String? = null       // 사용자가 해당 채팅방 방문여부 (Y/N)
){
    fun getUid(): String? {
        return uid
    }
    fun setUid(uid: String) {
        this.uid = uid
    }
    fun getChatId(): String? {
        return chatId
    }
    fun setChatId(chatId: String) {
        this.chatId = chatId
    }
    fun getChatName(): String? {
        return chatName
    }
    fun setChatName(chatName: String) {
        this.chatName = chatName
    }
    fun getLastMessage(): String? {
        return lastMessage
    }

    fun getChatImageUrl(): String? {
        return chatImageUrl
    }

    fun setMsgCnt(msgCnt: Long){
        this.msgCnt = msgCnt
    }

    fun getMsgCnt(): Long? {
        return msgCnt
    }
}

data class Message(var chatId:String = "", var message:String = "", var time:String = "" )


class ChatFragment : Fragment() {

    //private lateinit var data: MutableMap<String, String>
    private lateinit var data1: MutableMap<String, String>
    private lateinit var chat : MutableMap<String, String>

    val user = Firebase.auth.currentUser
    var uid : String? = null        // ?는 uid가 null일수도 있음을 표시

    companion object {
        fun newInstance() : ChatFragment {
            return ChatFragment()
        }
    }


    // 메모리에 올라갔을때
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    // 뷰가 생성되었을때
    // 프레그먼트와 레이아웃(xml)을 연결
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        // inflater : xml로 정의된 view를 실제 객체화 시키는 용도
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.chatFragment_recyclerview)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = RecyclerViewAdapter()

    }


    // 리사이클러뷰에 데이터 연결
    // adpater의 역할 : 데이터 리스트를 실제 눈으로 볼 수 있게 item으로 변환하는 중간다리 역할
    inner class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerViewAdapter.CustomViewHolder>(){

        // Firebase 데이터베이스에서 데이터를 수신하고 ArrayList를 채운다, 그리고 ArrayList가 recyclerview 어댑터의 데이터소르로 사용됩니다
        // 채팅방 리스트
        private var chatList: ArrayList<ChatListModel> = arrayListOf()
        private var message: ArrayList<Message> = arrayListOf()
        //private var uid : String? = null        // ?는 uid가 null일수도 있음을 표시

        private val destinationUsers : ArrayList<String> = arrayListOf()

        init {
            // 사용자의 uid
            uid = Firebase.auth.currentUser?.uid.toString()
            println("사용자 uid :$uid")

            setupChatList()



        }

        // 전체채팅방 목록 초기화 및 업데이트 처리
        private fun setupChatList(){
            if(user != null){

                // 사용자가 들어가있는 채팅방들
                // addListenerForSingleValueEvent -> 한번만 데이터를 가져오고 연결을 닫아버림
                // addValueEventListener -> db가 변경될때마다 호출
                database.child("chatList").child("$uid").addValueEventListener(object : ValueEventListener{
                    override fun onCancelled(error: DatabaseError) {}

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        chatList.clear()

                        for(snapshot in dataSnapshot.children){

                            //var msgCnt = snapshot.child("msgCnt").value as Long
                            var msgCnt: Long? = 0

                            data1 = snapshot.value as MutableMap<String, String>

                            Log.d("test", "delYn => " + data1.get("delYn"))

                            if("N".equals(data1.get("delYn"))){     // 나가지 않은 채팅방들만 리스트에 뿌려줌
                                Log.d("test", "chatId => " + snapshot.key.toString())
                                Log.d("test", "chatName => " + data1.get("chatName"))
                                Log.d("test", "lastMessage => " + data1.get("lastMessage"))

                                var chatId = snapshot.key
                                var chatName = data1.get("chatName").toString()
                                var lastMessage = ""
                                var chatImageUrl = ""
                                var visitYn = data1.get("visitYn").toString()


                                if(data1.get("lastMessage") != null){
                                    lastMessage = data1.get("lastMessage").toString()
                                }

                                if(data1.get("chatImageUrl") != null){
                                    chatImageUrl = data1.get("chatImageUrl").toString()
                                }

                                var delYn = data1.get("delYn")

                                Log.d("test", "msgCnt => " + msgCnt)

                                var myMutableList1: ArrayList<ChatListModel> = arrayListOf(
                                    ChatListModel(
                                        uid,
                                        chatId.toString(),
                                        chatName,
                                        lastMessage,
                                        chatImageUrl,
                                        delYn,
                                        msgCnt,
                                        visitYn
                                    )
                                )
                                chatList.addAll(myMutableList1)
                            }




                        }
                        notifyDataSetChanged()
                    }
                })

            }
        }





        // onCreateViewHolder는 RecyclerView의 아이템으로 만들어 두었던 list_item.xml을 LayoutInflater하여 뷰의 형태로 변환
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
            println("onCreateViewHolder!!!!!")

            return CustomViewHolder(LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false))
        }

        inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val imageView: ImageView = itemView.findViewById(R.id.chat_item_imageview)
            val textView_title : TextView = itemView.findViewById(R.id.chat_textview_title)
            val textView_lastMessage : TextView = itemView.findViewById(R.id.chat_item_textview_lastmessage)
            val textView_chatCount : TextView = itemView.findViewById(R.id.chat_item_textview_chatCount)
            val textView_chatDayTime : TextView = itemView.findViewById(R.id.chat_item_textview_chatDayTime)
        }

        // recyclerview가 viewholder를 가져와 데이터를 연결할때 호출
        // 적절한 데이터를 가져와서 그 데이터를 사용하여 뷰홀더의 레이아웃을 채움
        // 항목뷰에 데이터를 연결
        override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {

            println("onBindViewHolder!!!!!")

            var chatId = chatList[position].getChatId().toString()
            var chatName = chatList[position].getChatName().toString()
            var lastMessage = chatList[position].getLastMessage().toString()
            var chatImageUrl = chatList[position].getChatImageUrl()
            var msgCnt = chatList[position].getMsgCnt()


            if(chatImageUrl != null){
                println("chatImageUrl => " + chatImageUrl)

                Glide.with(holder.imageView.context).load(chatImageUrl.toString())
                    .error(R.drawable.user)     // 이미지 로드 실패시 기본이미지 셋팅
                    .circleCrop()
                    .into(holder.imageView)
            }

            database.child("chat").child(chatId).orderByChild("time").addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}

                override fun onDataChange(snapshot: DataSnapshot) {

                    if(snapshot.exists()){
                        holder.textView_lastMessage.text = snapshot.children.last().child("message").getValue().toString()
                        //holder.textView_chatDayTime.text = snapshot.children.last().child("time").getValue().toString()
                        //database.child("chatList").child(uid).child()

                        holder.textView_chatDayTime.text = getLastMessageTimeString(snapshot.children.last().child("time").getValue().toString())

                    }
                }

            })

            //holder.textView_lastMessage.text = lastMessage
            holder.textView_title.text = chatName


            // 안읽은 메시지가 0보다 크면 안읽은 메시지수를 보여줌
            if(msgCnt!! > 0) {
                Log.d("test", "msgCnt => "+ msgCnt)
                holder.textView_chatCount.visibility = View.VISIBLE
                holder.textView_chatCount.text = msgCnt.toString()
            }
            else{
                Log.d("test", "msgCnt => "+ 0)
                holder.textView_chatCount.visibility = View.GONE
            }



            //채팅창 선택시 이동
            holder.itemView.setOnClickListener {
                println("setOnClickListener!!!")

                Log.d("test", "chatId => " + chatId)

                var uid = user?.uid.toString()

                Log.d("test", "읽기전 msgCnt => " + msgCnt)

                // 해당채팅방 입장시 안읽은메시지 카운트 0으로 셋팅
                database.child("chatList").child(uid).child(chatId).child("msgCnt").setValue(0)

                val intent = Intent(activity, ChatActivity::class.java)
                intent.putExtra("chatId", chatId)
                startActivity(intent)
            }

            // 채팅방 길게 누르면 채팅방 퇴장유무 alertDialog
            holder.itemView.setOnLongClickListener {
                println("setOnLongClickListener!!!")

                val builder = AlertDialog.Builder(holder.itemView.context)
                builder.setTitle("채팅장 퇴장")
                    .setMessage("해당 채팅방을 퇴장하시겠습니까?")
                    .setPositiveButton("예",
                        DialogInterface.OnClickListener { dialog, id ->
                            var uid = user?.uid.toString()

                            database.child("chatList").child(uid).child(chatId).child("delYn").setValue("Y")
                            Toast.makeText(this@ChatFragment.context, "퇴장 성공", Toast.LENGTH_LONG).show()

                        })
                    .setNegativeButton("아니요",
                        DialogInterface.OnClickListener { dialog, id ->

                        })

                builder.create()
                builder.show()

                return@setOnLongClickListener(true)
            }

        }

        @SuppressLint("NewApi")
        private fun getLastMessageTimeString(lastTimeString: String): String {
            try {
                var currentTime = LocalDateTime.now().atZone(TimeZone.getDefault().toZoneId()) //현재 시각
                var dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")

                var messageMonth = lastTimeString.substring(4, 6).toInt()                   //마지막 메시지 시각 월,일,시,분
                var messageDate = lastTimeString.substring(6, 8).toInt()
                var messageHour = lastTimeString.substring(8, 10).toInt()
                var messageMinute = lastTimeString.substring(10, 12).toInt()

                var formattedCurrentTimeString = currentTime.format(dateTimeFormatter)     //현 시각 월,일,시,분
                var currentMonth = formattedCurrentTimeString.substring(4, 6).toInt()
                var currentDate = formattedCurrentTimeString.substring(6, 8).toInt()
                var currentHour = formattedCurrentTimeString.substring(8, 10).toInt()
                var currentMinute = formattedCurrentTimeString.substring(10, 12).toInt()

                var monthAgo = currentMonth - messageMonth                           //현 시각과 마지막 메시지 시각과의 차이. 월,일,시,분
                var dayAgo = currentDate - messageDate
                var hourAgo = currentHour - messageHour
                var minuteAgo = currentMinute - messageMinute

                if (monthAgo > 0)                                         //1개월 이상 차이 나는 경우
                    return monthAgo.toString() + "개월 전"
                else {
                    if (dayAgo > 0) {                                  //1일 이상 차이 나는 경우
                        if (dayAgo == 1)
                            return "어제"
                        else
                            return dayAgo.toString() + "일 전"
                    } else {
                        if (hourAgo > 0)
                            return hourAgo.toString() + "시간 전"     //1시간 이상 차이 나는 경우
                        else {
                            if (minuteAgo > 0)                       //1분 이상 차이 나는 경우
                                return minuteAgo.toString() + "분 전"
                            else
                                return "방금"
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return ""
            }
        }


        // 뿌려줄 데이터의 전체 길이를 리턴  (아이템갯수)
        override fun getItemCount(): Int {
            return chatList.size
        }


    }


}