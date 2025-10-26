package graphcalc;

public class Index {
    private int index;
    private String errorMessage;
    public Index(int index) {
        this.index = index;
        errorMessage = "";
    }

    public int getIndex() {
        return index;
    }

    public void updateIndex() {
        index--;
    }

    public void updateError(String message) {
        errorMessage = message;
    }

    public String getError() {
        return errorMessage;
    }
}
