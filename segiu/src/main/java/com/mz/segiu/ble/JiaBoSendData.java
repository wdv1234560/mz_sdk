package com.mz.segiu.ble;

import android.content.Context;

import com.mz.segiu.mvp.model.entity.PdaPrintEntity;
import com.tools.command.EscCommand;
import com.tools.command.LabelCommand;

import java.util.List;
import java.util.Vector;



public class JiaBoSendData {

    private static int labx = 50;//标签宽度
    private static int laby = 50;//标签高度
    private static int space = 0;//标签间隙
    private static int organx = 0;//设置原点坐标X
    private static int organy = 0;//设置原点坐标Y
    private static int x = 0;//位置x
    private static int y = 0;//位置y
    private static String d = "测试";//打印字体数据
    private static int s = 0;//二维码大小

    public static void sendLabel(Context context, PdaPrintEntity jsonObject, int id) throws Exception{
//        ArmsUtils.makeText("发送标签");
        LabelCommand tsc = new LabelCommand();
        /* 设置标签尺寸，按照实际尺寸设置 */
            labx = jsonObject.labx;

            laby = jsonObject.laby;
            space = jsonObject.space;
            organx = jsonObject.organx;
            organy = jsonObject.organy;
            tsc.addSize(labx, laby);
            /* 设置标签间隙，按照实际尺寸设置，如果为无间隙纸则设置为0 */
            tsc.addGap(space);
            /* 设置打印方向 */
            tsc.addDirection(LabelCommand.DIRECTION.FORWARD, LabelCommand.MIRROR.NORMAL);
            /* 开启带Response的打印，用于连续打印 */
            tsc.addQueryPrinterStatus(LabelCommand.RESPONSE_MODE.ON);
            /* 设置原点坐标 */
            tsc.addReference(organx, organy);
            /* 撕纸模式开启 */
            tsc.addTear(EscCommand.ENABLE.ON);
            /* 清除打印缓冲区 */
            tsc.addCls();
            /* 绘制简体中文 */
            List<PdaPrintEntity.Font> fontList = jsonObject.font;
            for (int i = 0; i < fontList.size(); i++) {
                PdaPrintEntity.Font font = fontList.get(i);
                tsc.addText(font.x, font.y, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,
                        font.d);
            }

//        /* 绘制图片 */
//        Bitmap b = BitmapFactory.decodeResource(null, R.drawable.gprinter);
//        tsc.addBitmap(10, 20, LabelCommand.BITMAP_MODE.OVERWRITE, 300, b);
            PdaPrintEntity.Qr qr = jsonObject.qr;
            /* 绘制二维码 */
            tsc.addQRCode(qr.x, qr.y, LabelCommand.EEC.LEVEL_L, qr.s, LabelCommand.ROTATION.ROTATION_0, qr.d);
//        /* 绘制一维条码 */
//        tsc.add1DBarcode(10, 450, LabelCommand.BARCODETYPE.CODE128, 100, LabelCommand.READABEL.EANBEL, LabelCommand.ROTATION.ROTATION_0, "SMARNET");

//        tsc.addText(10, 40, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,
//                "简体字");

//        tsc.addText(100, 580, LabelCommand.FONTTYPE.TRADITIONAL_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,
//                "繁體字");
//
//        tsc.addText(190, 580, LabelCommand.FONTTYPE.KOREAN, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,
//        "한국어");

            /* 打印标签 */
            tsc.addPrint(1, 1);
            /* 打印标签后 蜂鸣器响 */
            tsc.addSound(2, 100);
            tsc.addCashdrwer(LabelCommand.FOOT.F5, 255, 255);
            Vector<Byte> datas = tsc.getCommand();
            /* 发送数据 */
            if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id] == null) {
                Utils.toast(context, "打印机为空.");
                throw new IllegalStateException("打印机为空");
            }
            DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].sendDataImmediately(datas);
    }

}
