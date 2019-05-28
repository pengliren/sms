package com.sms.server;

import java.beans.ConstructorProperties;
import java.lang.ref.WeakReference;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientList <E> extends AbstractList<E> {

	private CopyOnWriteArrayList<WeakReference<E>> items = new CopyOnWriteArrayList<WeakReference<E>>();

	@ConstructorProperties(value = { "" })
	public ClientList() {
	}

	@ConstructorProperties({"c"})
	public ClientList(Collection<E> c) {
		addAll(0, c);
	}

	public boolean add(E element) {
		return items.add(new WeakReference<E>(element));
	}

	public void add(int index, E element) {
		items.add(index, new WeakReference<E>(element));
	}

	@Override
	public E remove(int index) {
		WeakReference<E> ref = items.remove(index);
		return ref.get();
	}

	@Override
	public boolean remove(Object o) {
		boolean removed = false;
		E element = null;
		for (WeakReference<E> ref : items) {
			element = ref.get();
			if (element != null && element.equals(o)) {
				ref.clear();
				removed = true;
				break;
			}
		}
		return removed;
	}

	@Override
	public boolean contains(Object o) {
		List<E> list = new ArrayList<E>();
		for (WeakReference<E> ref : items) {
			if (ref.get() != null) {
				list.add(ref.get());
			}
		}
		boolean contains = list.contains(o);
		list.clear();
		list = null;
		return contains;
	}

	public int size() {
		removeReleased();
		return items.size();
	}

	public E get(int index) {
		return (items.get(index)).get();
	}

	private void removeReleased() {
		for (WeakReference<E> ref : items) {
			if (ref.get() == null) {
				items.remove(ref);
			}
		}
	}
}
