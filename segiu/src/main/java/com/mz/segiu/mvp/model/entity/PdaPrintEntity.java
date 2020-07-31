package com.mz.segiu.mvp.model.entity;

import java.util.List;

public class PdaPrintEntity {
    public List<Font> font;
    public int labx;
    public int laby;
    public int organx;
    public int organy;
    public int space;
    public Qr qr;

    public static class Font {
        public String d;
        public int x;
        public int y;
    }

    public static class Qr {
        public String d;
        public int s;
        public int x;
        public int y;
    }

}


