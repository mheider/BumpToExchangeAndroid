package bump;

import java.util.Observer;

public interface IBumpDetector {
    void addBumpObserver(Observer observer);
}
