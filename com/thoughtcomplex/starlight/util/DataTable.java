package com.thoughtcomplex.starlight.util;

import java.util.ArrayList;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

public class DataTable<T> implements TableModel {
	
	ArrayList<ArrayList<T>> data;
	ArrayList<TableModelListener> listeners = new ArrayList<TableModelListener>();
	
	public DataTable() {
		data = new ArrayList<ArrayList<T>>();
	}
	
	public int getWidth() {
		int apparentWidth = 0;
		for(ArrayList<T> row : data) {
			if (row==null) continue;
			apparentWidth = Math.max(apparentWidth, row.size());
		}
		return apparentWidth;
	}
	
	public int getHeight() { return data.size(); }
	
	public T get(int x, int y) {
		if (x<0 | y<0) throw new IndexOutOfBoundsException();
		if (y>=data.size()) return null;
		ArrayList<T> row = data.get(y);
		if (row==null) return null;
		if (x>=row.size()) return null;
		return row.get(x);
	}

	public void set(int x, int y, T value) {
		if (x<0 | y<0) throw new IndexOutOfBoundsException();
		
		ArrayList<T> row;
		if (y>=data.size()) {
			while(data.size()<y+1) {
				data.add(new ArrayList<T>());
			}
			row = new ArrayList<T>(x+1);
			data.set(y, row);
		} else row = data.get(y);
		if (x>=row.size()) row.ensureCapacity(x+1);
		while(row.size()<x+1) row.add(null);
		row.set(x, value);
	}
	
	@Override
	public void addTableModelListener(TableModelListener l) {
		listeners.add(l);
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return Object.class;
	}

	@Override
	public int getColumnCount() {
		return getWidth();
	}

	@Override
	public String getColumnName(int columnIndex) {
		return "";
	}

	@Override
	public int getRowCount() {
		return getHeight();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return get(columnIndex,rowIndex);
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}

	@Override
	public void removeTableModelListener(TableModelListener l) {
		listeners.remove(l);
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		try {
			@SuppressWarnings("unchecked")
			T test = (T)aValue;
			set(columnIndex,rowIndex,test);
		} catch (ClassCastException ex) {
			throw new IllegalArgumentException("Cannot cast to internal data type from "+aValue.getClass().getName(),ex);
		}
	}
	
	/**
	 * Removes all elements from this collection.
	 */
	public void clear() {
		data.clear();
	}
}
