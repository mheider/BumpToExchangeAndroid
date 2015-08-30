package bump;


public class KalmanFilter {
    private final double K_FILTERING_FACTOR = 0.1;

    public void applyTo(Sample currentData, Sample oldData) {
        oldData.x = currentData.x - ((currentData.x * K_FILTERING_FACTOR) + (oldData.x * (1.0 - K_FILTERING_FACTOR)));
        oldData.y = currentData.y - ((currentData.y * K_FILTERING_FACTOR) + (oldData.y * (1.0 - K_FILTERING_FACTOR)));
        oldData.z = currentData.z - ((currentData.z * K_FILTERING_FACTOR) + (oldData.z * (1.0 - K_FILTERING_FACTOR)));
    }
}
