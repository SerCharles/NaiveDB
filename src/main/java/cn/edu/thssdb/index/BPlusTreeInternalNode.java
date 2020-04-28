package cn.edu.thssdb.index;

import cn.edu.thssdb.utils.Global;

import java.util.ArrayList;
import java.util.Collections;

public final class BPlusTreeInternalNode<K extends Comparable<K>, V> extends BPlusTreeNode<K, V> {

  ArrayList<BPlusTreeNode<K, V>> children;

  BPlusTreeInternalNode(int size) {
    keys = new ArrayList<>(Collections.nCopies((int) (1.5 * Global.fanout) + 1, null));
    children = new ArrayList<>((Collections.nCopies((int) (1.5 * Global.fanout) + 2, null)));
    this.nodeSize = size;
  }

  private void childrenAdd(int index, BPlusTreeNode<K, V> node) {
    for (int i = nodeSize + 1; i > index; i--) {
      children.set(i, children.get(i - 1));
    }
    children.set(index, node);
  }

  private void childrenRemove(int index) {
    for (int i = index; i < nodeSize; i++) {
      children.set(i, children.get(i + 1));
    }
  }

  @Override
  boolean containsKey(K key) {
    return searchChild(key).containsKey(key);
  }

  @Override
  V get(K key) {
    return searchChild(key).get(key);
  }

  @Override
  void put(K key, V value) {
    BPlusTreeNode<K, V> child = searchChild(key);
    child.put(key, value);
    if (child.isOverFlow()) {
      BPlusTreeNode<K, V> newSiblingNode = child.split();
      insertChild(newSiblingNode.getFirstLeafKey(), newSiblingNode);
    }
  }

  @Override
  void remove(K key) {
    int index = binarySearch(key);
    int childIndex = index >= 0 ? index + 1 : -index - 1;
    BPlusTreeNode<K, V> child = children.get(childIndex);
    child.remove(key);
    if (child.isUnderFlow()) {
      BPlusTreeNode<K, V> childLeftSibling = getChildLeftSibling(key);
      BPlusTreeNode<K, V> childRightSibling = getChildRightSibling(key);
      BPlusTreeNode<K, V> left = childLeftSibling != null ? childLeftSibling : child;
      BPlusTreeNode<K, V> right = childLeftSibling != null ? child : childRightSibling;
      left.merge(right);
      if (index >= 0) {
        childrenRemove(index + 1);
        keysRemove(index);
      } else {
        assert right != null;
        deleteChild(right.getFirstLeafKey());
      }
      if (left.isOverFlow()) {
        BPlusTreeNode<K, V> newSiblingNode = left.split();
        insertChild(newSiblingNode.getFirstLeafKey(), newSiblingNode);
      }
    } else if (index >= 0)
      keys.set(index, children.get(index + 1).getFirstLeafKey());
  }

  @Override
  K getFirstLeafKey() {
    return children.get(0).getFirstLeafKey();
  }

  @Override
  BPlusTreeNode<K, V> split() {
    int from = size() / 2 + 1;
    int to = size();
    BPlusTreeInternalNode<K, V> newSiblingNode = new BPlusTreeInternalNode(to - from);
    for (int i = 0; i < to - from; i++) {
      newSiblingNode.keys.set(i, keys.get(i + from));
      newSiblingNode.children.set(i, children.get(i + from));
    }
    newSiblingNode.children.set(to - from, children.get(to));
    this.nodeSize = this.nodeSize - to + from - 1;
    return newSiblingNode;
  }

  @Override
  void merge(BPlusTreeNode<K, V> sibling) {
    int index = nodeSize;
    BPlusTreeInternalNode<K, V> node = (BPlusTreeInternalNode<K, V>) sibling;
    int length = node.nodeSize;
    keys.set(index, node.getFirstLeafKey());
    for (int i = 0; i < length; i++) {
      keys.set(i + index + 1, node.keys.get(i));
      children.set(i + index + 1, node.children.get(i));
    }
    children.set(length + index + 1, node.children.get(length));
    nodeSize = index + length + 1;
  }

  private BPlusTreeNode<K, V> searchChild(K key) {
    int index = binarySearch(key);
    return children.get(index >= 0 ? index + 1 : -index - 1);
  }

  private void insertChild(K key, BPlusTreeNode<K, V> child) {
    int index = binarySearch(key);
    int childIndex = index >= 0 ? index + 1 : -index - 1;
    if (index >= 0) {
      children.set(childIndex, child);
    } else {
      childrenAdd(childIndex + 1, child);
      keysAdd(childIndex, key);
    }
  }

  private void deleteChild(K key) {
    int index = binarySearch(key);
    if (index >= 0) {
      childrenRemove(index + 1);
      keysRemove(index);
    }
  }

  private BPlusTreeNode<K, V> getChildLeftSibling(K key) {
    int index = binarySearch(key);
    int childIndex = index >= 0 ? index + 1 : -index - 1;
    if (childIndex > 0)
      return children.get(childIndex - 1);
    return null;
  }

  private BPlusTreeNode<K, V> getChildRightSibling(K key) {
    int index = binarySearch(key);
    int childIndex = index >= 0 ? index + 1 : -index - 1;
    if (childIndex < size())
      return children.get(childIndex + 1);
    return null;
  }
}
