package solver;
import java.util.*;

public class IndexedPriorityQueue<T> {
    private List<T> heap;
    private Map<T, Integer> indexMap;
    private Comparator<? super T> comparator;

    public IndexedPriorityQueue(Comparator<? super T> comparator) {
        this.heap = new ArrayList<>();
        this.indexMap = new HashMap<>();
        this.comparator = comparator;
    }

    public boolean contains(T element) {
        return indexMap.containsKey(element);
    }

    public void add(T element) {
        heap.add(element);
        int index = heap.size() - 1;
        indexMap.put(element, index);
        percolateUp(index);
    }

    public T poll() {
        if (heap.isEmpty()) {
            return null;
        }
        T minElement = heap.get(0);
        T lastElement = heap.remove(heap.size() - 1);
        indexMap.remove(minElement);

        if (!heap.isEmpty()) {
            heap.set(0, lastElement);
            indexMap.put(lastElement, 0);
            percolateDown(0);
        }
        return minElement;
    }

    public void remove(T element) {
        Integer index = indexMap.get(element);
        if (index == null) {
            return;
        }
        T lastElement = heap.remove(heap.size() - 1);
        indexMap.remove(element);

        if (index < heap.size()) {
            heap.set(index, lastElement);
            indexMap.put(lastElement, index);
            percolateDown(index);
            percolateUp(index);
        }
    }

    public void update(T element) {
        Integer index = indexMap.get(element);
        if (index != null) {
            percolateUp(index);
            percolateDown(index);
        }
    }

    public T getElement(T element) {
        Integer index = indexMap.get(element);
        if (index != null) {
            return heap.get(index);
        }
        return null;
    }

    public boolean isEmpty() {
        return heap.isEmpty();
    }

    // Helper methods for percolating up and down
    private void percolateUp(int index) {
        T element = heap.get(index);
        while (index > 0) {
            int parentIndex = (index - 1) / 2;
            T parent = heap.get(parentIndex);
            if (comparator.compare(element, parent) >= 0) {
                break;
            }
            heap.set(index, parent);
            indexMap.put(parent, index);
            index = parentIndex;
        }
        heap.set(index, element);
        indexMap.put(element, index);
    }

    private void percolateDown(int index) {
        int size = heap.size();
        T element = heap.get(index);
        while (true) {
            int leftChildIndex = 2 * index + 1;
            if (leftChildIndex >= size) {
                break;
            }
            int rightChildIndex = leftChildIndex + 1;

            int smallestChildIndex = leftChildIndex;
            T smallestChild = heap.get(leftChildIndex);

            if (rightChildIndex < size) {
                T rightChild = heap.get(rightChildIndex);
                if (comparator.compare(rightChild, smallestChild) < 0) {
                    smallestChildIndex = rightChildIndex;
                    smallestChild = rightChild;
                }
            }

            if (comparator.compare(element, smallestChild) <= 0) {
                break;
            }

            heap.set(index, smallestChild);
            indexMap.put(smallestChild, index);
            index = smallestChildIndex;
        }
        heap.set(index, element);
        indexMap.put(element, index);
    }
}

