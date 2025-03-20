package Entities;

public class Hours {
    private int starthour;
    private int endhour;
    public Hours(int starthour, int endhour) {
        this.starthour = starthour;
        this.endhour = endhour;
    }
    public int getStarthour() {
        return starthour;
    }
    public void setStarthour(int starthour) {
        this.starthour = starthour;
    }
    public int getEndhour() {
        return endhour;
    }
    public void setEndhour(int endhour) {
        this.endhour = endhour;
    }
    public String toString() {
        return starthour + ":" + endhour;
    }

}
