package iterator;

import java.util.List;
import java.util.NoSuchElementException;

public class ExtendedIteratorCities implements ExtendedIterator<String> {
    private List<String> cities;
    private int currentIndex;

    public ExtendedIteratorCities(List<String> cities) {
        this.cities = cities;
        this.currentIndex = -1; // Start before the first element for forward traversal
    }

    @Override
    public boolean hasNext() {
        return currentIndex < cities.size() - 1;
    }

    @Override
    public String next() {
        if (!hasNext()) throw new NoSuchElementException("No next element.");
        currentIndex++;
        return cities.get(currentIndex);
    }

    @Override
    public String previous() {
        if (!hasPrevious()) throw new NoSuchElementException("No previous element.");
        currentIndex--;
        return cities.get(currentIndex);
    }

    @Override
    public boolean hasPrevious() {
        return currentIndex > 0;
    }

    @Override
    public void goFirst() {
        currentIndex = -1;
    }

    @Override
    public void goLast() {
        currentIndex = cities.size();
    }
}
