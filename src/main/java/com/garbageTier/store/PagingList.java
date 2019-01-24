package com.garbageTier.store;

import java.util.*;

public class PagingList<T> implements List<T> {
    private List<T> contents;
    int pageSize;
    Iterator<T> currentPosition;

    public PagingList(List<T> initialContents, int pageSize) {
        contents = new ArrayList<>(initialContents);
        this.pageSize = pageSize;
        currentPosition = contents.iterator();
    }

    public List<T> nextPage() {
        List<T> results = new ArrayList<>();
        int currentCount = 0;

        while(currentCount < pageSize && currentPosition.hasNext()) {
            results.add(currentPosition.next());
            currentCount++;
        }

        return results;
    }

    public boolean hasNext() {
        return currentPosition.hasNext();
    }

    public void resetPosition() {
        currentPosition = contents.listIterator(0);
    }

    @Override
    public boolean isEmpty() {
        return contents.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @Override
    public Iterator<T> iterator() {
        return contents.iterator();
    }

    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return null;
    }

    @Override
    public boolean add(T t) {
        return contents.add(t);
    }

    @Override
    public boolean remove(Object o) {
        return contents.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return contents.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return contents.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        return contents.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return contents.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    @Override
    public void clear() {
        contents.clear();
    }

    @Override
    public T get(int index) {
        return null;
    }

    @Override
    public T set(int index, T element) {
        return null;
    }

    @Override
    public void add(int index, T element) {

    }

    @Override
    public T remove(int index) {
        return null;
    }

    @Override
    public int indexOf(Object o) {
        return 0;
    }

    @Override
    public int lastIndexOf(Object o) {
        return 0;
    }

    @Override
    public ListIterator<T> listIterator() {
        return null;
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return null;
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return null;
    }

    @Override
    public int size() {
        return contents.size();
    }
}
