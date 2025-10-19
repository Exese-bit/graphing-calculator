package graphcalc;

public class Index {
    private int index;
    public Index(int index) {
        this.index = index;
    }
    public int getIndex() {
        return index;
    }
    public void updateIndex() {
        index--;
    }
}
