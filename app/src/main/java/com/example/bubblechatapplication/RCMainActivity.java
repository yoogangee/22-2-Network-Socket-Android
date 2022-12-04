package com.example.bubblechatapplication;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class RCMainActivity extends AppCompatActivity {

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String msg, msg1, lastSender="";
    private String nickName, portStr, pp_CountStr = "0";
    private int port_num = 0;


    private LinearLayout nickContainer, portContainer, chatBubblesBox, bubbleContainer, sendContainer, chatReceive, header, header2;
    private RecyclerView recyclerView;
    private EditText editNick, editPort, editMsg;
    private Button btnOK, btnEnter, btnSend, btnRole1, btnRole2, btnRole3;
    private CheckBox btnHeart, heart;
    private ScrollView chatboxScroll;
    private TextView bubble, tv_sender, tv_guide1;

    private int doubleClickFlag = 0;
    private final long  CLICK_DELAY = 250;

    // RCView
    ArrayList<MessageModel> messagesList = new ArrayList<>();
    MessageViewAdapter adapter = new MessageViewAdapter(this, messagesList, out, nickName);



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rcmain);

        portContainer = (LinearLayout)findViewById(R.id.linearLayout_port);
        nickContainer = (LinearLayout) findViewById(R.id.linearLayout1);


        sendContainer = (LinearLayout)findViewById(R.id.linearLayout3);
        sendContainer.setVisibility(View.INVISIBLE);

        header = (LinearLayout)findViewById(R.id.linearLayout4);
        header2 = (LinearLayout)findViewById(R.id.linearLayout5);


        editNick = (EditText)findViewById(R.id.edit_nick);
        editPort = (EditText)findViewById(R.id.edit_port);
        editMsg = (EditText)findViewById(R.id.edit_msg);
        btnSend = (Button)findViewById(R.id.btn_send);

        btnRole1=(Button)findViewById(R.id.btn_role_1);
        btnRole2=(Button)findViewById(R.id.btn_role_2);
        btnRole3=(Button)findViewById(R.id.btn_role_3);

        editMsg.setEnabled(false);
        btnSend.setEnabled(false);

        // Recycler view item click event 처리
        adapter.setOnItemClickListener(new MessageViewAdapter.OnItemClickEventListener() {
            @Override
            public void onItemClick(View a_view, int a_position) {
                final MessageModel messageModel = messagesList.get(a_position);
                Log.d("CLICK", "clicked "+a_position);
//                // Toast.makeText(PhMainActivity.this, item.getName() + " Click event", Toast.LENGTH_SHORT).show();
//
//                messagesList.get(a_position).liked=1;
//
//                Log.d("CLICK", "liked "+messagesList.get(a_position).liked);
//
//                adapter.notifyItemChanged(a_position);
//                adapter.notifyDataSetChanged();
//
//                if (messageModel.senderType==5) {
//                    Log.d("EVENT", "doubleclicked");
//
//                    heart.setSelected(true);
//                    messageModel.setLiked(1);
//                    adapter.notifyItemChanged(a_position);
//
//                    String likeID = "[" + messageModel.sender +": "+getMsgContent(messageModel.msg) + "]";
//
//                    new Thread() {
//                        public void run() {
//                            sendMessage(getRole(messageModel.msg)+";♥:"+ nickName + "님이 출동합니다.\n" + likeID);
//                        }
//                    }.start();
//                }

//
//                doubleClickFlag++;
//                Handler handler = new Handler();
//                Runnable clickRunnable = new Runnable() {
//                    @Override
//                    public void run() {
//                        doubleClickFlag = 0;
//                        // todo click event
//                    }
//                };
//                if( doubleClickFlag == 1 ) {
//                    handler.postDelayed( clickRunnable, CLICK_DELAY);
//                }else if( doubleClickFlag == 2 ) {
//                    doubleClickFlag = 0;
//                    // todo 더블클릭 이벤트
//                    // 서버만 더블클릭 적용 가능
//                    if (messageModel.senderType==5) {
//                        Log.d("EVENT", "doubleclicked");
//
//                        heart.setBackgroundResource(R.drawable.heart_filled);
//                        adapter.notifyItemChanged(a_position);
//
//                        String likeID = "[" + messageModel.sender +": "+getMsgContent(messageModel.msg) + "]";
//
//                        new Thread() {
//                            public void run() {
//                                sendMessage(getRole(messageModel.msg)+";♥:"+ nickName + "님이 출동합니다.\n" + likeID);
//                            }
//                        }.start();
//                    }
//                }
            }
        });

        // RCView
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        btnEnter = (Button)findViewById(R.id.btn_enter);
        btnEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nickName = String.valueOf(editNick.getText());
                if (!nickName.equals("")) {
                    if (nickName.contains(":") || nickName.contains(" ") || nickName.length() > 4) {
                        Toast.makeText(getApplicationContext(),
                                "닉네임에는 : 문자나 공백을 포함할 수 없습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        portStr = String.valueOf(editPort.getText());
                        if (!portStr.equals("")) {
                            if (portStr.contains(":") || portStr.contains(" ")) {
                                Toast.makeText(getApplicationContext(),
                                        "포트 번호에는 : 문자나 공백을 포함할 수 없습니다.", Toast.LENGTH_SHORT).show();
                            } else {
                                new Thread() {
                                    public void run() {
                                        connect();
                                    }
                                }.start();
                            }
                        }
                    }
                }
            }
        });



        /* 역할 구분 */
        final int[] roleNum = {0};

        btnRole1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                roleNum[0] =1;
                Toast.makeText(RCMainActivity.this, "[중앙관리본부]를 선택하셨습니다.", Toast.LENGTH_SHORT).show();
//                Log.d("Role", String.valueOf(roleNum[0]));
            }
        });
        btnRole2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                roleNum[0] = 2;
                Toast.makeText(RCMainActivity.this, "[지역소방본부]를 선택하셨습니다.", Toast.LENGTH_SHORT).show();
            }
        });
        btnRole3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                roleNum[0] = 3;
                Toast.makeText(RCMainActivity.this, "[소방대원]을 선택하셨습니다.", Toast.LENGTH_SHORT).show();
            }
        });



        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!String.valueOf(editMsg.getText()).equals("")) {
                    msg1 = String.valueOf(roleNum[0]) + ";" + nickName + ":" + String.valueOf(editMsg.getText()) + "\n";
                    new Thread() {
                        public void run() {
                            sendMessage(msg1);  // 메시지 전송
                        }
                    }.start();
                    editMsg.setText("");
                }
            }
        });

    }
    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    @SuppressLint("ResourceAsColor")
    private void connect() {
        try {
            port_num= Integer.valueOf(portStr);
            if (port_num != 0)
                socket = new Socket("10.101.13.15", port_num);//String.valueOf(R.string.host), port_num);
            System.out.println("서버 연결됨.");


            //setTitle("포트: " + portStr);

            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());

            adapter.out=out;
            adapter.nickName=nickName;

            // 닉네임 설정
            out.writeUTF(nickName);
            System.out.println("클라이언트 : 메시지 전송완료");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // 메시지 작성 및 전송 기능 활성화
                    editMsg.setEnabled(true);
                    btnSend.setEnabled(true);

                    // 닉네임 입력 및 확인 기능 비활성화 + 숨기기
                    editNick.setEnabled(false);
                    editPort.setEnabled(false);
                    btnEnter.setEnabled(false);
                    btnRole1.setEnabled(false);
                    btnRole2.setEnabled(false);
                    btnRole3.setEnabled(false);
                    nickContainer.setVisibility(View.GONE);
                    portContainer.setVisibility(View.GONE);

//                    // "이곳에 채팅내용이 표시됩니다" 가이드 텍스트 숨기기
//                    tv_guide1.setVisibility(View.GONE);
//                    btnHeart.setVisibility(View.GONE);
                    header.setVisibility(View.GONE);
                    header2.setVisibility(View.GONE);

                    // 전송 레이아웃 보이게
                    sendContainer.setVisibility(View.VISIBLE);

                }
            });


            // 화면에 메시지 버블 출력
            while (in != null) {

                msg = removeLastEnter(in.readUTF());

                Log.d("MESSAGE", msg);

                if (isNotification(msg)) {  // 안내메시지
                    if (msg.contains("인원수:")) continue;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            messagesList.add(new MessageModel(msg, "서버", MessageViewAdapter.MESSAGE_TYPE_NOTI, -1));//
                            // notify adapter
                            adapter.notifyDataSetChanged();

                        }
                    });
                    // focus?
                    lastSender = "";
                    continue;
                }

                final String sender = whoIsSender(msg);
                final int role = Integer.parseInt(getRole(msg));
                MessageModel msgModal = new MessageModel(msg, sender, MessageViewAdapter.MESSAGE_TYPE_IN, role);


                if (!sender.equals(lastSender)) {
                    if (sender.equals(nickName)) {
                        msgModal.messageType=MessageViewAdapter.MESSAGE_TYPE_OUT;
                    }
                    else {
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!sender.equals(lastSender) && !sender.equals(nickName)) {
                            // 내가 아닐 때
                        }
                        if (sender.equals("서버")) {
//                            chatReceive.addView(heart);
                            // 서버일 때
                        }
                        else if (sender.equals(nickName)) {
                            msgModal.messageType=MessageViewAdapter.MESSAGE_TYPE_OUT;
                        }
                        if (!msg.contains("인원수:")){
                            messagesList.add(msgModal);
                            // notify adapter
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
                lastSender = sender;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String msg2) {
        try {
            out.writeUTF(msg2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setNickname(String nickName) {
        this.nickName = nickName;
    }

    private Boolean isNotification(String msg) {
        /*
            채팅이 아니라 정보알림일 경우, true
            "***님이 접속하셨습니다."
            "***님이 나가셨습니다."
        */
        return !msg.contains(";") || msg.contains("인원수") || msg.contains("접속하셨") ;
    }
    private String getRole(String msg0) {
        int indexColon = msg0.indexOf(";");  // msg에서 가장 먼저 나오는 : => 닉네임 규칙 - :를 포함하면 안됨!

        return msg0.substring(0, indexColon);
    }

    private String whoIsSender(String msg3) {
        int indexColon_role = msg3.indexOf(";");
        int indexColon = msg3.indexOf(":");  // msg에서 가장 먼저 나오는 : => 닉네임 규칙 - :를 포함하면 안됨!

        return msg3.substring(indexColon_role+1, indexColon);
    }

    private String getMsgContent(String msg4) {

        int indexColon = msg4.indexOf(":");  // msg에서 가장 먼저 나오는 : => 닉네임 규칙 - :를 포함하면 안됨!

        return msg4.substring(indexColon+1, msg4.length());
    }

    private String removeLastEnter(String msg5) {
        if (msg5.charAt(msg5.length()-1) == '\n') {
            return msg5.substring(0, msg5.length()-1);
        } else return msg5;
    }
}
