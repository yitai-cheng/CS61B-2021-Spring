package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T>, Iterable<T> {
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

    @Override
    public T get(int i) {
        return items[(nextFirst + 1 + i) % items.length];
    }

    @Override
    public void addFirst(T item) {
        if (size == items.length) {
            resize(items.length * 2);
        }
        items[nextFirst] = item;
        nextFirst = minusOne(nextFirst);
        size += 1;
    }

    @Override
    public void addLast(T item) {
        if (size == items.length) {
            resize(items.length * 2);
        }
        items[nextLast] = item;
        nextLast = (nextLast + 1) % items.length;
        size += 1;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void printDeque() {
        for (int i = 0; i < size; i++) {
            System.out.print(get(i) + " ");
        }
        System.out.println();
    }

    @Override
    public T removeFirst() {
        if (size == 0) {
            return null;
        }
        if (size >= 16 && (size - 1) < 0.25 * items.length) {
            resize(items.length / 2);
        }
        T removedItem = items[(nextFirst + 1) % items.length];
        nextFirst = (nextFirst + 1) % items.length;
        size -= 1;
        return removedItem;
    }

    @Override
    public T removeLast() {
        if (size == 0) {
            return null;
        }
        if (size >= 16 && (size - 1) < 0.25 * items.length) {
            resize(items.length / 2);
        }
        T removedItem = items[minusOne(nextLast)];
        nextLast = minusOne(nextLast);
        size -= 1;
        return removedItem;
    }

    private void resize(int newLength) {
        T[] newItems = (T[]) new Object[newLength];
        int start = (nextFirst + 1) % items.length;
        for (int i = 0; i < size; i++) {
            newItems[i] = items[start];
            start = (start + 1) % items.length;
        }
        items = newItems;
        nextFirst = items.length - 1;
        nextLast = size;
    }

    private int minusOne(int index) {
        if (index == 0) {
            return items.length - 1;
        } else {
            return index - 1;
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new ArrayDequeIterator();
    }

    private class ArrayDequeIterator implements Iterator<T> {
        private int index;

        public ArrayDequeIterator() {
            index = 0;
        }

        @Override
        public boolean hasNext() {
            return index < size();
        }

        @Override
        public T next() {
            T nextItem = get(index);
            index += 1;
            return nextItem;
        }
    }

    //    why can't I use @override here?
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof ArrayDeque<?>) {
            ArrayDeque<?> otherArrayDeque = (ArrayDeque<?>) o;
            if (this.size() != otherArrayDeque.size()) {
                return false;
            }
            for (int i = 0; i < size(); i++) {
                if (!this.get(i).equals(otherArrayDeque.get(i))) {
                    return false;
                }
            }
            return true;
        }
        return false;

    }

}
