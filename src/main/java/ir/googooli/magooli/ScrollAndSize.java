package ir.googooli.magooli;

public class ScrollAndSize {
    private int size;
    private String scroll;

    public ScrollAndSize(int size, String scroll) {
        this.size = size;
        this.scroll = scroll;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getScroll() {
        return scroll;
    }

    public void setScroll(String scroll) {
        this.scroll = scroll;
    }
}
