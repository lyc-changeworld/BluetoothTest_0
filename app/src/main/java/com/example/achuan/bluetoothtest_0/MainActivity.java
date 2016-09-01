package com.example.achuan.bluetoothtest_0;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

     private BluetoothAdapter bluetoothAdapter;
    BluetoothDevice device;
    Set<BluetoothDevice>bluetoothDevices;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //１－访问主机设备上的默认蓝牙适配器
         bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        //2-管理蓝牙属性和状态
        /*String toastText;
        if(bluetoothAdapter.isEnabled())//如果蓝牙适配器已经打开
        {
            //获取本地蓝牙的地址
            String  address=bluetoothAdapter.getAddress();
            //获取本地蓝牙的名称
            String  name=bluetoothAdapter.getName();
            toastText=name+":"+address;
            //如果拥有BLUETOOTH_ADMI权限，就可以更改蓝牙适配器的名称
            bluetoothAdapter.setName("lyc-changeworld");
        }
        else {
            toastText="Bluetooth is not enabled";
        }
        //int state=bluetoothAdapter.getState();//获取当前蓝牙适配器的状态
        Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();*/
        /*3-启用蓝牙适配器,可以通过将ACTION_REQUEST_ENABLE作为一个startActivityForResult操作字符串来使用*/
        /*String enableBT = BluetoothAdapter.ACTION_REQUEST_ENABLE;
        //跳转到一个子活动，它将提示用户打开蓝牙并请求确认
        startActivityForResult(new Intent(enableBT),0);//如果用户点击允许，系统将打开蓝牙适配器*/

        /*//4.1-使用意图提示用户启用蓝牙和广播接收器
        if(!bluetoothAdapter.isEnabled())
        {
            String actionStateChanged=BluetoothAdapter.ACTION_STATE_CHANGED;
            String actionRequestEnable=BluetoothAdapter.ACTION_REQUEST_ENABLE;
            //注册广播监听
            registerReceiver(
                    bluetoothState,//接收器类
                    new IntentFilter(actionStateChanged));//设置接收的意图是蓝牙适配器的状态发送改变
            //启动一个蓝牙打开请求的子活动，活动结束后会返回蓝牙适配器的状态的相关数据到前一个活动
            startActivityForResult(new Intent(actionRequestEnable),0);
        }*/
        /*5-可发现性和远程设备发现*/
        //5.1-管理设备的可发现性
        /*
        *获取适配器的扫描其它设备的模式：
        * SCAN_MODE_CONNECTABLE_DISCOVERABLE:启用查询扫描和页面扫描,本设备可被任何执行发现扫描的蓝牙设备发现
        * SCAN_MODE_CONNECTABLE:启用查询扫描但是禁用查询扫描,指先前连接并绑定过的设备可以在发现过程中被找到,但找不到新设备
        * SCAN_MODE_NONE:可发现性关闭,任何设备都不能找到本地适配器
        * */
        //int scanMode=bluetoothAdapter.getScanMode();
        //显式提醒用户打开可发现机制
        if(!bluetoothAdapter.isEnabled())//如果蓝牙未打开
        {
            int DISCOVERY_REQUEST=1;//设置一个请求码
            String aDiscoverable=BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE;
            //注册广播监听
            registerReceiver(bluetoothScanMode,new IntentFilter(
                    BluetoothAdapter.ACTION_SCAN_MODE_CHANGED));
            startActivityForResult(new Intent(aDiscoverable),DISCOVERY_REQUEST);//跳转时携带上请求码
        }
        //5.2-发现远程设备
        if(!bluetoothAdapter.isDiscovering())//如果没有启动发现机制
        {
            bluetoothAdapter.startDiscovery();//启动发现机制
        }
        //bluetoothAdapter.startDiscovery();//启动发现过程
        //bluetoothAdapter.cancelDiscovery();//取消发现过程
        //发现过程是异步进行的，使用广播意图来通知启动和结束发现过程以及发现远程设备
        //5.2.1-对发现机制进行广播监听（开始和结束）
        registerReceiver(discoveryMonitor,new
                IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
        registerReceiver(discoveryMonitor, new
                IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        //5.2.2-发现远程蓝牙设备
        //对蓝牙的发现设备的事件进行广播监听
        registerReceiver(discoveryResult, new
                IntentFilter(BluetoothDevice.ACTION_FOUND));
        //6-蓝牙通信
        //指定用户想要连接到的远程蓝牙设备的硬件地址
        /*BluetoothDevice device=bluetoothAdapter.getRemoteDevice("");
        //获得当前已经配对的设备集合
        Set<BluetoothDevice>bluetoothDevices=bluetoothAdapter.getBondedDevices();
        if(bluetoothDevices.contains(device)){
            //TODO:目标设备已经与局部服务绑定／配对
        }*/
        //6.1-检查远程设备的发现机制和配对
        //传入我的红米note增强版的蓝牙物理地址
        device=bluetoothAdapter.getRemoteDevice("7C:1D:D9:15:8A:6E");
        bluetoothDevices=bluetoothAdapter.getBondedDevices();
        //6.2-打开一个客户端蓝牙套结字连接，创建一个新连接，即没有配对过的设备
        /*BluetoothSocket socket=device.createInsecureRfcommSocketToServiceRecord(uuid);
        socket.connect();*/
        //传入接受该请求的ServerSocketUUID,并通过connect()的方法进行连接
        //6.3.1-使用Bluetooth Socket传输数据
        //使用Output Stream(输出数据流)和Input Stream(输入数据流)监听传入的字符串

    }
    //6.3.3-接收字符串
    private String listenForMessage()
    {
        String result="";
        int bufferSize=1024;//代表一字节的数据量
        byte[] buffer=new byte[bufferSize];

        try{
            //InputStream inputStream=socket.getInputStream();
            int bytesRead=-1;//当信道中数据为不可使用的数据时，读取到的值为-1
            while (true)
            {
                //bytesRead=inputStream.read(buffer)
                if(bytesRead!=-1)
                {
                    //遍历数据流，将数据按照单字节的单位传递到message对象中
                    while ((bytesRead==bufferSize)&&(buffer[bufferSize-1]!=0))
                    {
                        //mseeage=message+new String(buffer,0,bytesRead);
                        //bytesRead=inputStream.read(buffer);
                    }
                    //message=message+new String(buffer,0,bytesRead-1);//添加结束位数据
                    return result;
                }
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return result;
    }

    //6.3.2-发送字符串
    private void sendMessage(String msssage)
    {
        OutputStream outputStream;
        //拿到信道输出数据流
        //outputStream=socket.getOutputStream();
        //添加一个停止字符
        byte[] byteArray=(msssage+"").getBytes();
        byteArray[byteArray.length-1]=0;
        //outputStream.write(byteArray);//将要传输的数据信息变成数据流来传递

    }
    //5.2.2监听获取远程蓝牙设备的信息
    BroadcastReceiver discoveryResult=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String remoteDeviceName=intent.getStringExtra
                    (BluetoothDevice.EXTRA_NAME);//获取远程设备的名称
            //获取远程蓝牙设备的服务类,包含了设备的属性值
            BluetoothDevice bluetoothDevice;
            bluetoothDevice=intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Toast.makeText(context, "Discovered:"+remoteDeviceName
                    , Toast.LENGTH_SHORT).show();
            //TODO:处理远程蓝牙设备
            //6.1-检查设备的发现机制和配对
            //如果目标设备已被配对,并且可被发现
            if((bluetoothDevice.equals(device))&&(bluetoothDevices.contains(bluetoothDevice)))
            {
                    //TODO:
            }
        }
    };
    //5.2.1监听发现机制
    BroadcastReceiver discoveryMonitor=new BroadcastReceiver() {
        //意图的类型
        String dStarted=BluetoothAdapter.ACTION_DISCOVERY_STARTED;
        String dFinished=BluetoothAdapter.ACTION_DISCOVERY_FINISHED;
        @Override
        public void onReceive(Context context, Intent intent) {
            //根据不同的意图进行不同的响应
            if(dStarted.equals(intent.getAction()))//如果是启动了发现机制
            {
                Toast.makeText(context, "DiscoveryStarted", Toast.LENGTH_SHORT).show();
            }
            else if (dFinished.equals(intent.getAction()))//如果发现机制已经完成了
            {
                Toast.makeText(context, "DiscoveryCompleted", Toast.LENGTH_SHORT).show();
            }
        }
    };
    //5.1.2编写一个接收器来监听可发现机制模式
    BroadcastReceiver bluetoothScanMode=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {//可发现机制的模式
            String PrevScanMode=BluetoothAdapter.EXTRA_PREVIOUS_SCAN_MODE;//以前的模式
            String ScanMode=BluetoothAdapter.EXTRA_SCAN_MODE;//当前的模式
            int scanmode=intent.getIntExtra(ScanMode,-1);//获取广播发送者传递过来的模式数据
            int prevmode=intent.getIntExtra(PrevScanMode,-1);
            Toast.makeText(context, scanmode, Toast.LENGTH_SHORT).show();
        }
    };
    //5.1.1-跳转的子活动结束后回到主活动时会携带上数据,通过核对请求码来判断数据来源
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==1)//核对请求码,对子活动中传递过来的数据进行处理
        {
            boolean isDiscoverable=resultCode>0;//如果用户拒绝了子活动中的请求,返回的将是负值,否则为正值
            //resultCode参数指示了可发现机制的持续时间
            int discoverableDuration=resultCode;
            if(isDiscoverable)//如果打开了发现机制
            {
                //传入用来识别服务器的字符串name和uuid
                UUID uuid=UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
                String name="bluetoothserver";
                try {
                    //建立一个监听套接字以初始化设备之间的链路，需要其中一台设备充当服务器进行监听，并接受传入的连接请求
                    final BluetoothServerSocket bluetoothServerSocket=
                            bluetoothAdapter.listenUsingRfcommWithServiceRecord(name,uuid);
                    //开启一个子线程进行accept处理，该处理是一种阻塞操作
                    Thread acceptThread=new Thread(new Runnable() {
                        @Override
                        public void run() {
                            //在客户端连接建立以前保持阻塞
                            //BluetoothSocket：用于创建一个新的客户端套接字，以便连接到正在监听的BluetoothServerSocke
                            try {
                                BluetoothSocket socket=bluetoothServerSocket.accept();
                                //TODO:使用服务器套接字传输数据
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    acceptThread.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    //4.2-建立一个广播接收器来监听蓝牙适配器的状态
    BroadcastReceiver bluetoothState=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String prevStateExtra=BluetoothAdapter.EXTRA_PREVIOUS_STATE;//当前状态的标志名
            String stateExtra=BluetoothAdapter.EXTRA_STATE;//状态的标志名
            int state=intent.getIntExtra(stateExtra,-1);//获取广播传递过来的状态数据
            int previousState=intent.getIntExtra(prevStateExtra,-1);
            String toast="";
            switch (state)
            {
                case (BluetoothAdapter.STATE_TURNING_ON)://如果当前适配器正在打开
                {
                    toast="Bluetooth turning on";break;//提示正在打开
                }
                case (BluetoothAdapter.STATE_ON)://如果适配器已经打开
                {
                    toast="Bluetooth on";
                    unregisterReceiver(this);//适配器打开就关闭这个广播接收器监听者
                    break;//提示已经打开
                }
                case (BluetoothAdapter.STATE_TURNING_OFF)://如果适配器已经打开
                {
                    toast="Bluetooth turning off";
                    unregisterReceiver(this);//如果适配器已经打开就关闭广播接收器监听
                    break;//提示已经打开
                }
                case (BluetoothAdapter.STATE_OFF)://如果适配器已经打开
                {
                    toast="Bluetooth off";
                    unregisterReceiver(this);//如果适配器已经打开就关闭广播接收器监听
                    break;//提示已经打开
                }
                default:break;
            }
            Toast.makeText(context, toast, Toast.LENGTH_SHORT).show();
        }
    };

}
