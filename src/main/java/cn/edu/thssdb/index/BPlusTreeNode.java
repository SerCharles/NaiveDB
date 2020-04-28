package cn.edu.thssdb.index;

import cn.edu.thssdb.utils.Global;

import java.util.ArrayList;
import java.util.Collections;

abstract class BPlusTreeNode<K extends Comparable<K>, V> {
  ArrayList<K> keys;
  int nodeSize;

  abstract V get(K key);

  abstract void put(K key, V value);

  abstract void remove(K key);

  abstract boolean containsKey(K key);

  abstract K getFirstLeafKey();

  abstract BPlusTreeNode<K, V> split();

  abstract void merge(BPlusTreeNode<K, V> sibling);

  int size() {
    return nodeSize;
  }

  boolean isOverFlow() {
    return nodeSize > Global.fanout - 1;
  }

  boolean isUnderFlow() {
    return nodeSize < (Global.fanout + 1) / 2 - 1;
  }

  int binarySearch(K key) {
    return Collections.binarySearch(keys.subList(0, nodeSize), key);
  }

  void keysAdd(int index, K key) {
    for (int i = nodeSize; i > index; i--) {
      keys.set(i, keys.get(i - 1));
    }
    keys.set(index, key);
    nodeSize++;
  }

  void keysRemove(int index) {
    for (int i = index; i < nodeSize - 1; i++) {
      keys.set(i, keys.get(i + 1));
    }
    nodeSize--;
  }
}
