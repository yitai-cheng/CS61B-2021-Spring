package deque;

import static java.lang.System.arraycopy;

public class ArrayDeque<T> {
    private int size;
    private T[] items;
    private int nextFirst;
    private int nextLast;

    public ArrayDeque() {
        size = 0;
        items = (T[]) new Object[8];
        nextFirst = items.length - 1;
        nextLast = size;
    }

    public T get(int i) {
        return items[(nextFirst + 1 + i) % items.length];
    }

    public void addFirst(T item) {
        if (size == items.length) {
            resizeBigger(items.length * 2);
        }
        items[nextFirst] = item;
        nextFirst = (nextFirst - 1) % items.length;
        size += 1;
    }

    public void addLast(T item) {
        if (size == items.length) {
            resizeBigger(items.length * 2);
        }
        items[nextLast] = item;
        nextLast = (nextLast + 1) % items.length;
        size += 1;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public void printDeque() {
        for (int i = 0; i < size; i++) {
            System.out.print(get(i) + " ");
        }
        System.out.println();
    }

    public T removeFirst() {
        if (size == 0) {
            return null;
        }
        if (size >= 16 && size - 1 < 0.25 * items.length) {
            resizeSmaller(items.length / 2);
        }
        T removedItem = items[(nextFirst + 1) % items.length];
        nextFirst = (nextFirst + 1) % items.length;
        size -= 1;
        return removedItem;
    }

    public T removeLast() {
        if (size == 0) {
            return null;
        }
        if (size >= 16 && size - 1 < 0.25 * items.length) {
            resizeSmaller(items.length / 2);
        }
        T removedItem = items[(nextLast - 1) % items.length];
        nextLast = (nextLast - 1) % items.length;
        size -= 1;
        return removedItem;
    }

    private void resizeBigger(int newLength) {
        T[] newItems = (T[]) new Object[newLength];
        arraycopy(items, 0, newItems, 0, items.length);
        items = newItems;
        nextFirst = items.length - 1;
        nextLast = size;
    }

    private void resizeSmaller(int newLength) {
        T[] newItems = (T[]) new Object[newLength];
        int start = nextFirst + 1;
        for (int i = 0; i < size; i++) {
            newItems[i] = items[start];
            start = (start + 1) % items.length;
        }
        items = newItems;
        nextFirst = items.length - 1;
        nextLast = size;
    }


}
