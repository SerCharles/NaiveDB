package cn.edu.thssdb.index;

import javafx.util.Pair;

import java.util.Iterator;
import java.util.LinkedList;

public class BPlusTreeIterator<K extends Comparable<K>, V> implements Iterator<Pair<K, V>> {
  private LinkedList<BPlusTreeNode<K, V>> queue;
  private LinkedList<Pair<K, V>> buffer;

  BPlusTreeIterator(BPlusTree<K, V> tree) {
    queue = new LinkedList<>();
    buffer = new LinkedList<>();
    if (tree.size() == 0)
      return;
    queue.add(tree.root);
  }

  @Override
  public boolean hasNext() {
    return !queue.isEmpty() || !buffer.isEmpty();
  }

  @Override
  public Pair<K, V> next() {
    if (buffer.isEmpty()) {
      while (true) {
        BPlusTreeNode<K, V> node = queue.poll();
        if (node instanceof BPlusTreeLeafNode) {
          for (int i = 0; i < node.size(); i++)
            buffer.add(new Pair<>(node.keys.get(i), ((BPlusTreeLeafNode<K, V>) node).values.get(i)));
          break;
        } else if (node instanceof BPlusTreeInternalNode)
          for (int i = 0; i <= node.size(); i++)
            queue.add(((BPlusTreeInternalNode<K, V>) node).children.get(i));
      }
    }
    return buffer.poll();
  }
}
