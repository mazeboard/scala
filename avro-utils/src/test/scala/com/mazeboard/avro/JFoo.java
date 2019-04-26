package com.mazeboard.avro;

public class JFoo extends JBar {
    public Integer a;
    public JBar b;

    public Integer getA() { return a; }

    public void setA(Integer x) {
        a = x;
    }
    public JBar getB() { return b; }

    public void setB(JBar x) {
        x.setBar("done");
        b = x;
    }
    public String toString() { return "JFoo("+a+","+b+","+getBar()+")"; }
}