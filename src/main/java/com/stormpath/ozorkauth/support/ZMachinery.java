package com.stormpath.ozorkauth.support;

import com.zaxsoft.zmachine.ZCPU;
import com.zaxsoft.zmachine.ZUserInterface;

import java.awt.Dimension;
import java.awt.Point;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

public class ZMachinery implements ZUserInterface {

    ZCPU cpu;
    Thread cpuThread;
    InputStream in;
    OutputStream out;
    String saveFile;

    public ZMachinery(String zFile, InputStream in, OutputStream out, String saveFile) {
        this.in = in;
        this.out = out;
        this.saveFile = saveFile;

        cpu = new ZCPU(this);
        cpu.initialize(zFile);
        cpuThread = cpu.start();
    }

    @Override
    public void fatal(String errmsg) {

    }

    @Override
    public void initialize(int ver) {

    }

    @Override
    public void setTerminatingCharacters(Vector chars) {

    }

    @Override
    public boolean hasStatusLine() {
        return false;
    }

    @Override
    public boolean hasUpperWindow() {
        return false;
    }

    @Override
    public boolean defaultFontProportional() {
        return false;
    }

    @Override
    public boolean hasColors() {
        return false;
    }

    @Override
    public boolean hasBoldface() {
        return false;
    }

    @Override
    public boolean hasItalic() {
        return false;
    }

    @Override
    public boolean hasFixedWidth() {
        return false;
    }

    @Override
    public boolean hasTimedInput() {
        return false;
    }

    @Override
    public Dimension getScreenCharacters() {
        return null;
    }

    @Override
    public Dimension getScreenUnits() {
        return null;
    }

    @Override
    public Dimension getFontSize() {
        return null;
    }

    @Override
    public Dimension getWindowSize(int window) {
        return null;
    }

    @Override
    public int getDefaultForeground() {
        return 0;
    }

    @Override
    public int getDefaultBackground() {
        return 0;
    }

    @Override
    public Point getCursorPosition() {
        return null;
    }

    @Override
    public void showStatusBar(String s, int a, int b, boolean flag) {

    }

    @Override
    public void splitScreen(int lines) {

    }

    @Override
    public void setCurrentWindow(int window) {

    }

    @Override
    public void setCursorPosition(int x, int y) {

    }

    @Override
    public void setColor(int fg, int bg) {

    }

    @Override
    public void setTextStyle(int style) {

    }

    @Override
    public void setFont(int font) {

    }

    @Override
    public int readLine(StringBuffer sb, int time) {
        int rc;

        while ((rc = readChar(time)) != -1 && rc != '\n') {
            sb.append((char)rc);
        }

        // this is a hack
        // When the ByteArrayInputStream is exhausted, we just want to tie things up here until the thread is killed.
        if (rc == -1) {
            while (true) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        return '\n';
    }

    @Override
    public int readChar(int time) {
        try {
            return in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void showString(String s) {
        try {
            out.write(s.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void scrollWindow(int lines) {

    }

    @Override
    public void eraseLine(int s) {

    }

    @Override
    public void eraseWindow(int window) {

    }

    @Override
    public String getFilename(String title, String suggested, boolean saveFlag) {
        return saveFile;
    }

    @Override
    public void quit() {
        Thread curThread = cpuThread;
        cpuThread = null;
        curThread.stop();
    }

    @Override
    public void restart() {

    }
}
