package com.example.achuan.bluetoothtest_0;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by achuan on 16-8-30.
 */
public class BluetoothTexting extends AppCompatActivity {
    private BluetoothAdapter bluetooth;
    private BluetoothSocket socket;
    //定义一个UUID以识别用户的应用程序
    private UUID uuid=UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    //设置一个请求码
    private static int DISCOVERY_REQUEST=1;
    //创建一个集合存储所发现的蓝牙设备
    private ArrayList<String> foundDevices=new ArrayList<String>();
    private ArrayList<BluetoothDevice> foundHardDevices=new ArrayList<BluetoothDevice>();
    //private ArrayList<BluetoothDevice> foundDevices=new ArrayList<BluetoothDevice>();
    //创建一个数组适配器将listView与所找到的设备数组绑定
    //private ArrayAdapter<BluetoothDevice> aa;
    private ArrayAdapter<String> aa;
    private ListView list;
    //主线程操作者
    private android.os.Handler handler=new android.os.Handler();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_main);
        //1-获得蓝牙Adapter
        configureBluetooth();
        //2-设置已发现设备的ListView
        setupListView();
        //3-设置搜索按钮
        setupSearchButton();
        if(!bluetooth.isEnabled())//如果蓝牙未打开
        {
            //显式提醒用户打开可发现机制
            Intent disc=new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            disc.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,300);
            startActivityForResult(disc,DISCOVERY_REQUEST);//跳转到子活动,并带上一个请求码
        }
        registerReceiver(connectedDeviceReceiver,new IntentFilter(
                BluetoothDevice.ACTION_ACL_CONNECTED));//监听连接事件
        registerReceiver(connectedDeviceReceiver,new IntentFilter(
                BluetoothDevice.ACTION_ACL_DISCONNECTED));//监听断开连接事件
    }
    //连接设备广播的接收者类
    BroadcastReceiver connectedDeviceReceiver=new BroadcastReceiver() {
        //意图的类型
        String connected=BluetoothDevice.ACTION_ACL_CONNECTED;
        String disconnected=BluetoothDevice.ACTION_ACL_DISCONNECTED;
        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();//获取广播的意图
            BluetoothDevice remoteDevice;
            //获取远程蓝牙设备的服务类,包含了设备的属性值
            remoteDevice=intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            //根据不同的意图进行不同的响应
            if(connected.equals(action))//如果连上了设备
            {
                Toast.makeText(context, "已经连接到"+remoteDevice.getName(), Toast.LENGTH_SHORT).show();
            }
            else if (disconnected.equals(action))//如果发现机制已经完成了
            {
                Toast.makeText(context, "蓝牙已断开连接", Toast.LENGTH_SHORT).show();
            }
        }
    };
    //7-发现设备机制的开启
    private void setupSearchButton() {
        Button searchButton= (Button) findViewById(R.id.button_search);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //为找到远程蓝牙设备的意图注册监听器
                registerReceiver(discoveryResult//相应意图的接收器
                        ,new IntentFilter(BluetoothDevice.ACTION_FOUND));//配置意图为找到设置
                //如果发现机制没有启动
                if(!bluetooth.isDiscovering()){
                    foundDevices.clear();//清空发现设备的集合,重新开始存储
                    bluetooth.startDiscovery();//启动发现机制
                }
            }
        });
    }
    //6-创建一个新的广播接收器来监听蓝牙设备的发现设备广播
    BroadcastReceiver discoveryResult=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();//获取广播的意图
            BluetoothDevice remoteDevice;
            //获取远程蓝牙设备的服务类,包含了设备的属性值
            remoteDevice=intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            //已经配对过的设备：bluetooth.getBondedDevices().contains(remoteDevice)
            // 搜索设备时，取得设备的MAC地址
            if(BluetoothDevice.ACTION_FOUND.equals(action))
            {
                //String str=remoteDevice.getAddress();//先获取设备的MAC地址
                String str=remoteDevice.getName();
                /*//如果设备还没配对过
                if (remoteDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                   str=str+"未配对";
                }
                else {//配对过就显示已经配对
                    str=str+"已配对";
                }*/
                //如果发现的设备还未存储在设备列表中
                if(!foundHardDevices.contains(remoteDevice))
                {
                    foundHardDevices.add(remoteDevice);
                    foundDevices.add(str); // 获取mac地址和是否配对的消息
                    aa.notifyDataSetChanged();//更新列表显示
                }
            }
            /*else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action))
            {
                switch (remoteDevice.getBondState()) {
                    case BluetoothDevice.BOND_BONDING:
                        Log.d("BlueToothTestActivity", "正在配对......");
                        break;
                    case BluetoothDevice.BOND_BONDED:
                        Log.d("BlueToothTestActivity", "完成配对");
                        connect(device);//连接设备
                        break;
                    case BluetoothDevice.BOND_NONE:
                        Log.d("BlueToothTestActivity", "取消配对");
                    default:
                        break;
                }
            }*/
        }
    };
    //5-列表的配置
    private void setupListView() {
        list= (ListView) findViewById(R.id.list_discovered);
        //配置适配器
        /*aa=new ArrayAdapter<BluetoothDevice>(this,
                android.R.layout.simple_list_item_1,
                foundDevices);*/
        aa=new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                foundDevices);
        list.setAdapter(aa);//为设备列表添加数组适配器
        /***８-为列表中的item点击事件配置监听***/
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view,  int i, long l) {
                //为配对事件和UI更新添加一个异步消息处理方法
                AsyncTask<Integer,Void ,Void> connectTask=new AsyncTask<Integer, Void, Void>() {
                    @Override
                    protected Void doInBackground(Integer... integers) {
                        try {
                            //获取蓝牙设备的实例操作对象
                            BluetoothDevice device=foundHardDevices.get(integers[0]);
                            socket=device.createRfcommSocketToServiceRecord(uuid);
                            socket.connect();//为没有配对过的设备执行配对操作
                        } catch (IOException connectException) {
                            /*// Unable to connect; close the socket and get out
                            try {
                                socket.close();
                            } catch (IOException closeException) { }*/
                        }
                        return null;
                    }
                    @Override
                    protected void onPostExecute(Void aVoid) {
                        switchUI();//添加视图的显示方法
                        //Toast.makeText(BluetoothTexting.this, "start connect", Toast.LENGTH_SHORT).show();
                        super.onPostExecute(aVoid);
                    }
                };
                //启动这个异步消息处理的方法
                connectTask.execute(i);//i代表进度值
            }
        });
    }
    //4-添加活动销毁后返回的回调方法
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==DISCOVERY_REQUEST)//核对请求码,对子活动中传递过来的数据进行处理
        {
            boolean isDiscoverable=resultCode>0;//如果用户拒绝了子活动中的请求,返回的将是负值,否则为正值
            if(isDiscoverable)//如果打开了发现机制
            {
                String name="bluetoothserver";
                try {
                    //建立一个监听套接字以初始化设备之间的链路，需要其中一台设备充当服务器进行监听，并接受传入的连接请求
                    final BluetoothServerSocket bluetoothServerSocket=
                            bluetooth.listenUsingRfcommWithServiceRecord(name,uuid);
                    //开启一个子线程进行accept处理，该处理是一种阻塞操作
                    /*用异步消息处理机制来处理连接请求(子线程)和UI更新(主线程)*/
                    AsyncTask<Integer,Void,BluetoothSocket> acceptThread=new AsyncTask<Integer, Void, BluetoothSocket>() {
                        //在子线程中运行连接请求
                        @Override
                        protected BluetoothSocket doInBackground(Integer... integers) {
                                try {
                                    socket=bluetoothServerSocket.accept(integers[0]*1000);
                                    return socket;
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                /*//如果通道已经打开一个了,将其关闭后才能打开另一个
                                if(socket!=null)
                                {
                                    try {
                                        bluetoothServerSocket.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                }*/
                            return null;
                        }
                        //进行UI操作
                        @Override
                        protected void onPostExecute(BluetoothSocket bluetoothSocket) {
                            /*
                            将doInBackground后台方法中return返回的数据传入该方法
                            利用返回的数据进行UI操作
                            */
                            if(bluetoothSocket!=null)
                            {
                                switchUI();//一旦建立连接就进行视图控制
                            }
                            super.onPostExecute(bluetoothSocket);
                        }
                    };
                    acceptThread.execute(resultCode);//启动这个异步消息处理线程方法
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    //2-启用用于读取和写入消息的视图
    private void switchUI() {
         Button sendBtn= (Button) findViewById(R.id.send_msg_bt);
         TextView messageText= (TextView) findViewById(R.id.text_messages);
         final EditText textEntry= (EditText) findViewById(R.id.text_message);
        //messageText.setVisibility(View.VISIBLE);//让编辑框显示可见
        //list.setVisibility(View.GONE);//让列表消失且不占位置
        //textEntry.setEnabled(true);//让编辑框可编辑
        /***9-对edit text进行单击事件监听***/
        /*textEntry.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if((keyEvent.getAction()==keyEvent.ACTION_DOWN)
                        &&(i==keyEvent.KEYCODE_DPAD_CENTER))
                {
                    sendMessage(socket,textEntry.getText().toString());//将输入的文本发出去
                    textEntry.setText("");//清空编辑框
                    return  true;
                }
                return false;
            }
        });*/
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage(socket,textEntry.getText().toString());//将输入的文本发出去
                textEntry.setText("");//清空编辑框
            }
        });
        //新建接收消息监听实例
        BluetoothSocketListener bsl=new BluetoothSocketListener(
                socket,messageText,handler);
        Thread messageListener=new Thread(bsl);//新建接收消息监听的线程
        messageListener.start();//启动这个线程
    }
    //12-子线程进行数据的接收监听,然后让主线程来处理接收到的数据,并更新UI
    private class BluetoothSocketListener implements Runnable{
        private BluetoothSocket socket;
        private TextView textView;
        private android.os.Handler handler;
        public BluetoothSocketListener(BluetoothSocket socket, TextView textView, android.os.Handler handler) {
            this.socket = socket;
            this.textView = textView;
            this.handler = handler;
        }
        @Override
        public void run() {
            int bufferSize=1024;//代表一字节的数据量
            byte[] buffer=new byte[bufferSize];
            try{
                InputStream inputStream=socket.getInputStream();
                int bytesRead= -1;//当信道中数据为不可使用的数据时，读取到的值为-1
                String message="";
                while (true)
                {
                    bytesRead=inputStream.read(buffer);
                    if(bytesRead!=-1)
                    {
                        //遍历数据流，将数据按照单字节的单位传递到message对象中
                        while ((bytesRead==bufferSize)&&(buffer[bufferSize-1]!=0))
                        {
                            //1024个数据点组成一个字节的字符,再存入数据串中
                            message=message+new String(buffer,0,bytesRead);
                            bytesRead=inputStream.read(buffer);
                        }
                        message=message+new String(buffer,0,bytesRead-1);//最后添加结束位数据
                        //将更新UI的任务传递给主线程来做
                        handler.post(new MessagePoster(textView,message));
                        socket.getInputStream();//获得输入的数据流
                    }
                }
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    //11-将传入的消息发送给后台线程的UI
    private class MessagePoster implements Runnable{
        private TextView textView;
        private String message;
        public MessagePoster(TextView textView, String message) {
            this.textView = textView;
            this.message = message;
        }
        @Override
        public void run() {
            textView.setText(message);
        }
    }
    //10-通过信道发送字符串给远程蓝牙设备的方法
    private void sendMessage(BluetoothSocket socket,String msg)
    {
        OutputStream outputStream;//创建一个输出流
        try {
            //拿到信道输出数据流
            outputStream= socket.getOutputStream();
            //注意这里(msg+" ")带个空字符,为每个消息的结束为空字符,否则将丢失消息中的最后一个字符
            byte[] byteArray=(msg+" ").getBytes();
            //添加一个停止字符
            byteArray[byteArray.length-1]=0;
            //将要发送的数据变成流
            outputStream.write(byteArray);//将要传输的数据信息变成输出数据流来传递
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //１－配置蓝牙
    private void configureBluetooth() {
        bluetooth=BluetoothAdapter.getDefaultAdapter();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(discoveryResult);//活动销毁时注销监听注册
        unregisterReceiver(connectedDeviceReceiver);
    }
}
