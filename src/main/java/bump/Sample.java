package bump;

public class Sample implements Cloneable {
    public double x;
    public double y;
    public double z;

    public Sample(float[] values, double adjustmentFactor) {
        this.x = values[0] * adjustmentFactor;
        this.y = values[1] * adjustmentFactor;
        this.z = values[2] * adjustmentFactor;
    }

    public Sample(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Delta delta(Sample other) {
        return new Delta(delta(this.x, other.x), delta(this.y, other.y), delta(this.z, other.z));
    }

    private double delta(double left, double right) {
        if (left < 0 || right < 0) {
            return Math.abs(left) + Math.abs(right);
        } else {
            return Math.abs(Math.abs(left) - Math.abs(right));
        }
    }

    @Override
    public Sample clone() {
        return new Sample(this.x, this.y, this.z);
    }
}
