package com.sms.compatibility.messages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.io.amf3.IDataInput;
import com.sms.io.amf3.IDataOutput;
import com.sms.io.amf3.IExternalizable;

/**
 * Flex <code>ArrayCollection</code> compatibility class.
 * 
 * @see <a href="http://help.adobe.com/en_US/FlashPlatform/reference/actionscript/3/mx/collections/ArrayCollection.html">ArrayCollection</a>
 * @param <T> type of collection
 */
public class ArrayCollection<T> implements Collection<T>, List<T>, IExternalizable {

	private static final Logger log = LoggerFactory.getLogger(ArrayCollection.class);

	private ArrayList<T> source;
	
	public ArrayCollection() {
		this.source = new ArrayList<T>();
	}

	public ArrayCollection(T[] source) {
		this.source = new ArrayList<T>(source.length);
		this.source.addAll(Arrays.asList(source));
	}

	public void setSource(T[] source) {
		this.source = new ArrayList<T>(source.length);
		this.source.addAll(Arrays.asList(source));		
	}
	
	public int size() {
		return source.size();
	}

	public boolean isEmpty() {
		return source == null ? true : source.isEmpty();
	}

	public boolean contains(Object o) {
		return source.contains(o);
	}

	public Iterator<T> iterator() {
		return source.iterator();
	}

	@SuppressWarnings("unchecked")
	public T[] toArray() {
		return (T[]) source.toArray();
	}

	@SuppressWarnings("hiding")
	public <T> T[] toArray(T[] a) {
		return source.toArray(a);
	}

	public boolean add(T e) {
		return source.add(e);
	}

	public boolean remove(Object o) {
		return source.remove(o);
	}

	public boolean containsAll(Collection<?> c) {
		return source.containsAll(c);
	}

	public boolean addAll(Collection<? extends T> c) {
		return source.addAll(c);
	}

	public boolean removeAll(Collection<?> c) {
		return source.removeAll(c);
	}

	public boolean retainAll(Collection<?> c) {
		return source.retainAll(c);
	}

	public void clear() {
		if (source != null) {
			source.clear();
		}
	}

	public boolean addAll(int index, Collection<? extends T> c) {
		return source.addAll(index, c);
	}

	public T get(int index) {
		return source.get(index);
	}

	public T set(int index, T element) {
		return source.set(index, element);
	}

	public void add(int index, T element) {
		source.add(index, element);
	}

	public T remove(int index) {
		return source.remove(index);
	}

	public int indexOf(Object o) {
		return source.indexOf(o);
	}

	public int lastIndexOf(Object o) {
		return source.lastIndexOf(o);
	}

	public ListIterator<T> listIterator() {
		return source.listIterator();
	}

	public ListIterator<T> listIterator(int index) {
		return source.listIterator(index);
	}

	public List<T> subList(int fromIndex, int toIndex) {
		return source.subList(fromIndex, toIndex);
	}	

	@SuppressWarnings("unchecked")
	public void readExternal(IDataInput input) {
		log.debug("readExternal");
		if (source == null) {
			source = (ArrayList<T>) input.readObject();
		} else {
			source.clear();
			source.addAll((ArrayList<T>) input.readObject());
		}
	}

	public void writeExternal(IDataOutput output) {
		log.debug("writeExternal");
		output.writeObject(source);
	}

}
