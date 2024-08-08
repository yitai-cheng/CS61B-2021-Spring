package deque;

import java.util.Iterator;

public class LinkedListDeque<T> {
    private int size;
    private ListNode sentinal;

    public LinkedListDeque() {
        this.size = 0;
        this.sentinal = new ListNode(null);
        this.sentinal.next = this.sentinal;
        this.sentinal.prev = this.sentinal;
    }

    public void addFirst(T item) {
        ListNode newNode = new ListNode(item);
        newNode.prev = sentinal;
        newNode.next = sentinal.next;
        sentinal.next.prev = newNode;
        sentinal.next = newNode;
        size += 1;
    }

    public void addLast(T item) {
        ListNode newNode = new ListNode(item);
        newNode.prev = sentinal.prev;
        newNode.next = sentinal;
        sentinal.prev.next = newNode;
        sentinal.prev = newNode;
        size += 1;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public int size() {
        return size;
    }

    public void printDeque() {
        ListNode currentNode = sentinal;
        while (currentNode.next != sentinal) {
            currentNode = currentNode.next;
            System.out.print(currentNode + " ");
        }
        System.out.println();
    }

    public T removeFirst() {
        if (sentinal.next == sentinal) {
            return null;
        }
        ListNode toBeRemoved = sentinal.next;
        sentinal.next = sentinal.next.next;
        sentinal.next.prev = sentinal;
        size -= 1;
        return toBeRemoved.item;
    }

    public T removeLast() {
        if (sentinal.next == sentinal) {
            return null;
        }
        ListNode toBeRemoved = sentinal.prev;
        sentinal.prev = sentinal.prev.prev;
        sentinal.prev.next = sentinal;
        size -= 1;
        return toBeRemoved.item;
    }


    public T get(int index) {
        if (index >= size) {
            return null;
        }
        ListNode node = sentinal;
        for (int i = 0; i < index; i++) {
            node = node.next;
        }
        return node.next.item;
    }

    public T getRecursive(int index) {
        if (index >= size) {
            return null;
        }
        return getRecursiveHelper(index, sentinal);
    }

    private T getRecursiveHelper(int index, ListNode p) {
        if (index == 0) {
            return p.item;
        }
        return getRecursiveHelper(index - 1, p.next);
    }

    public Iterator<T> iterator() {
        return new LinkedListIterator();
    }

    private class LinkedListIterator implements Iterator<T> {
        private int index;
        public LinkedListIterator() { index = 0; }
        public boolean hasNext() { return index < size; }
        public T next() {
            T returnItem = get(index);
            index += 1;
            return returnItem;
        }
    }


    public boolean equals(Object o) {
        if (!(o instanceof LinkedListDeque) || ((LinkedListDeque<?>) o).size() != size()) {
            return false;
        }
        if (isEmpty()) {
            return true;
        }
        ListNode currentNode = sentinal.next;
        ListNode currentONode = (ListNode) ((LinkedListDeque<?>) o).sentinal;
        while (currentNode != sentinal) {
            if (!currentNode.item.equals(currentONode.item)) {
                return false;
            }
            currentNode = currentNode.next;
            currentONode = currentONode.next;
        }
        return true;
    }

    public class ListNode {
        ListNode prev;
        ListNode next;
        T item;

        public ListNode(T item) {
            this.item = item;
        }

        public ListNode(T item, ListNode prev, ListNode next) {
            this.item = item;
            this.prev = prev;
            this.next = next;
        }
    }

}