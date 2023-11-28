package com.scrat.app.richtext;

public class Part {
    private int start;
    private int end;

    public Part(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public boolean isValid() {
        return start < end;
    }
}
