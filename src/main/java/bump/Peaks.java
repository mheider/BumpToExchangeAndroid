package bump;

import java.util.ArrayList;

public class Peaks {
    public int x;
    public int y;
    public int z;

    public static Peaks readFrom(ArrayList<Sample> samples) {
        Peaks peaks = new Peaks();
        for (int i = 1; i < samples.size() - 1; i++) {

            Sample value = samples.get(i);
            Sample before = samples.get(i - 1);
            Sample next = samples.get(i + 1);

            if (value.x < before.x && value.x < next.x || value.x > before.x && value.x > next.x) {
                peaks.x++;
            }

            if (value.y < before.y && value.y < next.y || value.y > before.y && value.y > next.y) {
                peaks.y++;
            }

            if (value.z < before.z && value.z < next.z || value.z > before.z && value.z > next.z) {
                peaks.z++;
            }
        }

        return peaks;
    }

    public boolean between(int minPeaks, int maxPeaks) {
        return x >= minPeaks && x <= maxPeaks &&
               y >= minPeaks && y <= maxPeaks &&
               z >= minPeaks && z <= maxPeaks;
    }
}
