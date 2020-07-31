package com.mz.segiu.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.jess.arms.integration.EventBusManager;
import com.mz.segiu.mvp.model.entity.TcScaleEntity;
import com.mz.segiu.mvp.model.entity.ThPrintEntity;

import timber.log.Timber;

public class BleProtocol {
    public final static String TAG = BleProtocol.class.getSimpleName();
    private Context mContext;

    public BleProtocol(){

    }
    public BleProtocol(Context context){
        mContext = context;
    }

    /**
     * 解析台秤重量
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void onCharacteristicWrite(BluetoothGatt gatt,
                                      BluetoothGattCharacteristic characteristic) {
   			/*Log.e(TAG,"onCharWrite "+gatt.getDevice().getName()
   					+" write "
   					+characteristic.getUuid().toString()
   					+" -> "
   					+new String(characteristic.getValue()));
   					*/
        byte[] buffer =characteristic.getValue();
        int bytes=buffer.length;
        byte[] strdata = new byte[bytes];
        int pStrdata=0;
        for(int i = 0; i < bytes; i++)
        {
            if(buffer[i]==0x2)pStrdata=0;
            else  if(buffer[i]==0x0d)
            {
                GetWeight(strdata);
                pStrdata=0;
            }
            else
            {
                strdata[pStrdata] = buffer[i];
                if(++pStrdata>=30)pStrdata=0;
            }


        }

    }

    public void GetWeight(byte[] databuf)
    {
        TcScaleEntity scale = new TcScaleEntity();
        int i,j,offset=6;
        boolean	StartFalg=false;
        scale.bZeroFlag=true;
        scale.bOverFlag=false;
        scale.bWeiStaFlag=false;
        switch(databuf[0])
        {
            case 'o':
            case 'O':
                scale.bOverFlag=true;
                break;
            case 'u':
            case 'U':
                scale.bWeiStaFlag=false;
                offset=6;	//6
                break;
            case 's':
            case 'S':
                scale.bWeiStaFlag=true;
                break;
        }
        if(databuf[5]=='-')offset=5;
        for(i=0;i<14;i++)
        {
            if(databuf[i+offset]=='\'')databuf[i+offset]='.';
            if(StartFalg)
            {
                if(((databuf[i+offset]>'9')||(databuf[i+offset]<'.'))&&(!((databuf[i+offset]==' ')&&(databuf[i+offset+1]<='9'))))
                {
                    break;
                }
            }
            else if((databuf[i+offset]>='0')&&(databuf[i+offset]<='9'))
            {
                StartFalg=true;
                if(databuf[i+offset]!='0')scale.bZeroFlag=false;
            }
        }
        scale.sformatNetWeight=new String(databuf,offset,i);


        for(j=0;j<6;j++)
        {
            if(databuf[i+j+offset]<0x20)
            {
                break;
            }
        }
        scale.sUnit=new String(databuf,i+offset,j);

        Timber.d("台秤重量==" + scale.sformatNetWeight + scale.sUnit);
        EventBusManager.getInstance().post(scale.sformatNetWeight,"thWeight");
    }

    public String getThPrintModel(ThPrintEntity data){
        Timber.d(data.toString());
        String str="v1:"+data.orgName+"\\r\\n" +
                "n2:科室\\r\\nv2:"+data.getDeskWorkName()+"\\r\\n" +
                "n3:医废类型\\r\\nv3:"+data.getMedicalName()+"\\r\\n" +
                "n4:重量\\r\\nv4:"+data.getWeight()+"kg\\r\\n" +
                "n5:录入人\\r\\nv5:"+data.getUserName()+"\\r\\n" +
                "n6:交接人\\r\\nv6:"+data.getHeirName()+"\\r\\n" +
                "n7:时间\\r\\nv7:"+data.getDate()+"\\r\\n" +
                "v8:医废编码:\\r\\n" +
                "v9:"+data.getMedicalWasteCode()+"\\r\\n" +
                "print0\\r\\n";
        return str;
    }
    public String getPrintMsg(ThPrintEntity data){
        String th="{\n" +
                "\t\"labx\": 50,\n" +
                "\t\"laby\": 50,\n" +
                "\t\"space\": 0,\n" +
                "\t\"organx\": 0,\n" +
                "\t\"organy\": 0,\n" +
                "\t\"font\": [\n" +
                "\t\t{\n" +
                "\t\t\t\"x\": 10,\n" +
                "\t\t\t\"y\": 70,\n" +
                "\t\t\t\"d\": \""+data.getOrgName()+"\"\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"x\": 10,\n" +
                "\t\t\t\"y\": 100,\n" +
                "\t\t\t\"d\": \"科室："+data.getMedicalName()+"\"\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"x\": 10,\n" +
                "\t\t\t\"y\": 130,\n" +
                "\t\t\t\"d\": \"医废类型："+data.getMedicalName()+"\"\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"x\": 10,\n" +
                "\t\t\t\"y\": 160,\n" +
                "\t\t\t\"d\": \"重量："+data.getWeight()+"kg\"\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"x\": 10,\n" +
                "\t\t\t\"y\": 190,\n" +
                "\t\t\t\"d\": \"录入人："+data.getUserName()+"\"\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"x\": 10,\n" +
                "\t\t\t\"y\": 220,\n" +
                "\t\t\t\"d\": \"交接人："+data.getHeirName()+"\"\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"x\": 10,\n" +
                "\t\t\t\"y\": 250,\n" +
                "\t\t\t\"d\": \"时间："+data.getDate()+"\"\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"x\": 10,\n" +
                "\t\t\t\"y\": 280,\n" +
                "\t\t\t\"d\": \"医废编码：\"\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"x\": 10,\n" +
                "\t\t\t\"y\": 310,\n" +
                "\t\t\t\"d\": \"11111\"\n" +
                "\t\t}\n" +
                "\t],\n" +
                "\t\"qr\": {\n" +
                "\t\t\"x\": 270,\n" +
                "\t\t\"y\": 150,\n" +
                "\t\t\"d\": \""+data.getMedicalWasteCode()+"\",\n" +
                "\t\t\"s\": 4\n" +
                "\t}\n" +
                "}" ;
    
//    String printData = "{\n" +
//                "\t\"labx\": 50,\n" +
//                "\t\"laby\": 50,\n" +
//                "\t\"space\": 0,\n" +
//                "\t\"organx\": 0,\n" +
//                "\t\"organy\": 0,\n" +
//                "\t\"font\": [\n" +
//                "\t\t{\n" +
//                "\t\t\t\"x\": 10,\n" +
//                "\t\t\t\"y\": 240,\n" +
//                "\t\t\t\"d\": \"${data.organizationName}\"\n" +
//                "\t\t},\n" +
//                "\t\t{\n" +
//                "\t\t\t\"x\": 10,\n" +
//                "\t\t\t\"y\": 270,\n" +
//                "\t\t\t\"d\": \"入库批次码：\"\n" +
//                "\t\t},\n" +
//                "\t\t{\n" +
//                "\t\t\t\"x\": 10,\n" +
//                "\t\t\t\"y\": 300,\n" +
//                "\t\t\t\"d\": \"${data.batchNo}\"\n" +
//                "\t\t}\n" +
//                "\t],\n" +
//                "\t\"qr\": {\n" +
//                "\t\t\"x\": 120,\n" +
//                "\t\t\"y\": 60,\n" +
//                "\t\t\"d\": \"${data.batchNo}\",\n" +
//                "\t\t\"s\": 7\n" +
//                "\t}\n" +
//                "}";
        return th;
    }

}
