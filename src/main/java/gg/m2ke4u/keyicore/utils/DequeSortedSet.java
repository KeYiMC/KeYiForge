package gg.m2ke4u.keyicore.utils;

import java.util.*;

/**
 * 一个简单的用队列实现的Set，插入其中的数据顺序保持不变
 * 不支持Comparator，仅用于计划刻部分,并且线程不安全
 * @param <E>
 */
public class DequeSortedSet<E> implements SortedSet<E>{

    private final Deque<E> baking;

    public DequeSortedSet(Collection<E> input){
        this.baking = new ArrayDeque<>(input);
    }

    public DequeSortedSet(){
        this.baking = new ArrayDeque<>();
    }

    @Deprecated
    @Override
    public Comparator<? super E> comparator() {
        return null;
    }

    @Deprecated
    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        return null;
    }

    @Deprecated
    @Override
    public SortedSet<E> headSet(E toElement) {
        return null;
    }

    @Deprecated
    @Override
    public SortedSet<E> tailSet(E fromElement) {
        return null;
    }

    @Override
    public E first() {
        return this.baking.peekFirst();
    }

    @Override
    public E last() {
        return this.baking.peekLast();
    }

    @Override
    public int size() {
        return this.baking.size();
    }

    @Override
    public boolean isEmpty() {
        return this.baking.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return this.baking.contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        return this.baking.iterator();
    }

    @Override
    public Object[] toArray() {
        return this.baking.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return this.baking.toArray(a);
    }

    @Override
    public boolean add(E e) {
        return this.baking.add(e);
    }

    @Override
    public boolean remove(Object o) {
        return this.baking.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return this.baking.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return this.baking.addAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return this.baking.retainAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return this.baking.removeAll(c);
    }

    @Override
    public void clear() {
        this.baking.clear();
    }
}
