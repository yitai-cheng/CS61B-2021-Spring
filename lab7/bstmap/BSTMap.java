package bstmap;

import java.util.Iterator;
import java.util.Set;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {
    private BSTNode root;
    private int size;
    private class BSTNode {
        K key;
        V value;
        BSTNode left, right;
        BSTNode(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }
    @Override
    public void clear() {
        this.root = null;
        this.size = 0;
    }

    @Override
    public boolean containsKey(K key) {
        return containsKey(key, this.root);
    }

    private boolean containsKey(K key, BSTNode node) {
        if (node == null) {
            return false;
        }
        int cmp = key.compareTo(node.key);
        if (cmp == 0) {
            return true;
        }
        else if (cmp < 0) {
            return containsKey(key, node.left);
        }
        else {
            return containsKey(key, node.right);
        }
    }

    @Override
    public V get(K key) {
        return get(key, this.root);
    }

    private V get(K key, BSTNode node) {
        if (node == null) {
            return null;
        }
        int cmp = key.compareTo(node.key);
        if (cmp == 0) {
            return node.value;
        }
        else if (cmp < 0) {
            return get(key, node.left);
        }
        else {
            return get(key, node.right);
        }
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public void put(K key, V value) {
        this.size += 1;
        this.root = put(key, value, this.root);
    }

    private BSTNode put(K key, V value, BSTNode node) {
        if (node == null) {
            return new BSTNode(key, value);
        }
        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            node.left =  put(key, value, node.left);
        }
        else {
            node.right = put(key, value, node.right);
        }
        return node;
    }

    @Override
    public Set<K> keySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(K key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<K> iterator() {
        throw new UnsupportedOperationException();
    }

    public void printInOrder() {
        printInOrder(this.root);
    }
    private void printInOrder(BSTNode node) {
        if (node == null) {
            return;
        }
        printInOrder(node.left);
        System.out.println(node.key);
        printInOrder(node.right);
    }
}
