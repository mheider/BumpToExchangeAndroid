package bump;

public class Delta extends Sample {
    public Delta(double x, double y, double z) {
        super(x, y, z);
    }

    public boolean exceedsThreshold() {
        return this.x > 0.3 && this.y > 0.5 && this.z > 0.1;
    }
}
