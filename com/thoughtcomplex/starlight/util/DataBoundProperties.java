package com.thoughtcomplex.starlight.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Properties;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

public class DataBoundProperties
							extends Dictionary<String,String>
							implements TableModel, BoundData, Iterable<Entry<String,String>>, Streamable  {
	Properties dataSource;
	private ArrayList<TableModelListener> listeners = new ArrayList<TableModelListener>();
	private ArrayList<BoundDataListener> boundDataListeners = new ArrayList<BoundDataListener>();
	private ArrayList<SimpleEntry<String, String>> data = new ArrayList<SimpleEntry<String,String>>();
	boolean caseSensitive = false;
	
	public DataBoundProperties() {}
	
	@Override
	public void addTableModelListener(TableModelListener l) {
		listeners.add(l);
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return String.class;
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public String getColumnName(int columnIndex) {
		if (columnIndex==0) return "Property";
		if (columnIndex==1) return "Value";
		throw new IndexOutOfBoundsException("Invalid column");
	}

	@Override
	public int getRowCount() {
		return data.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex<0 | rowIndex>=data.size()) throw new IndexOutOfBoundsException("Invalid row index.");
		if (columnIndex<0 | columnIndex>=2) throw new IndexOutOfBoundsException("Invalid column index.");
		
		if (columnIndex==0) return data.get(rowIndex).getKey();
		if (columnIndex==1) return data.get(rowIndex).getValue();
		
		throw new UnsupportedOperationException("An unexpected error happened.");
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
		String stringValue = "";
		if (aValue instanceof String) stringValue = (String)aValue;
		else stringValue = aValue.toString();
		
		if (rowIndex<0 | rowIndex>=data.size()) throw new IndexOutOfBoundsException("Invalid row index.");
		if (columnIndex<0 | columnIndex>=2) throw new IndexOutOfBoundsException("Invalid column index.");
		
		if (columnIndex==0) {
			String priorValue = data.get(rowIndex).getValue();
			data.set(rowIndex, new SimpleEntry<String,String>(stringValue,priorValue));
			fireDataChangeNotification();
			return;
		}
		if (columnIndex==1) {
			if (data.get(rowIndex)==null) {
				data.set(rowIndex, new SimpleEntry<String,String>(stringValue,""));
				fireDataChangeNotification();
				return;
			}
			data.get(rowIndex).setValue(stringValue);
			fireDataChangeNotification();
			return;
		}
	}

	

	@Override
	public String get(Object o) {
		for(SimpleEntry<String,String> row : data) {
			if (caseSensitive) {
				if (row.getKey().equals(o)) return row.getValue();
			} else {
				if (row.getKey().equalsIgnoreCase(o.toString())) return row.getValue();
			}
		}
		
		return null;
	}

	@Override
	public boolean isEmpty() {
		return data.isEmpty();
	}

	@Override
	public Enumeration<String> keys() {
		return new DataBoundPropertiesEnumeration(data, true);
	}

	@Override
	public Enumeration<String> elements() {
		return new DataBoundPropertiesEnumeration(data, false);
	}
	
	@Override
	public String put(String k, String v) {
		for(SimpleEntry<String,String> row : data) {
			if (caseSensitive) {
				if (row.getKey().equals(k)) {
					String oldValue = row.getValue();
					row.setValue(v);
					for(TableModelListener listener : listeners) listener.tableChanged(new TableModelEvent(this));
					return oldValue;
				}
			} else {
				if (row.getKey().equalsIgnoreCase(k)) {
					String oldValue = row.getValue();
					row.setValue(v);
					for(TableModelListener listener : listeners) listener.tableChanged(new TableModelEvent(this));
					return oldValue;
				}
			}
		}
		
		data.add(new SimpleEntry<String,String>(k,v));
		fireDataChangeNotification();
		return null;
	}

	@Override
	public String remove(Object k) {
		int rowToRemove = -1;
		String removedValue = null;
		
		for(int i=0; i<data.size();i++) {
			SimpleEntry<String,String> row = data.get(i);
			
			if (caseSensitive) {
				if (row.getKey().equals(k)) {
					removedValue = row.getValue();
					rowToRemove = i;
				}
			} else {
				if (row.getKey().equalsIgnoreCase(k.toString())) {
					removedValue = row.getValue();
					rowToRemove = i;
				}
			}
		}
		
		if (rowToRemove>=0) {
			data.remove(rowToRemove);
			fireDataChangeNotification();
			return removedValue;
		} else return null;
	}

	@Override
	public int size() {
		return data.size();
	}
	
	@Override
	public Iterator<Entry<String,String>> iterator() {
		return new DataBoundPropertiesIterator(this);
	}
	
	private class DataBoundPropertiesEnumeration implements Enumeration<String> {

		private ArrayList<SimpleEntry<String,String>> data;
		private boolean enumeratesKeys = true;
		private int index = 0;
		
		private DataBoundPropertiesEnumeration(ArrayList<SimpleEntry<String,String>> data, boolean enumeratesKeys) {
			this.data = data;
			this.enumeratesKeys = enumeratesKeys;
		}
		
		@Override
		public boolean hasMoreElements() {
			return index<data.size();
		}

		@Override
		public String nextElement() {
			if (index>=data.size()) throw new NoSuchElementException();
			String cur = "";
			if (enumeratesKeys) cur = data.get(index).getKey();
			else cur=data.get(index).getValue();
			index++;
			return cur;
		}	
	}
	
	private class DataBoundPropertiesIterator implements Iterator<Entry<String,String>> {

		private DataBoundProperties data;
		private int index = 0;
		
		private DataBoundPropertiesIterator(DataBoundProperties data) {
			this.data = data;
		}
		
		@Override
		public boolean hasNext() {
			return index<data.size();
		}

		@Override
		public Entry<String,String> next() {
			if (index>=data.data.size()) throw new NoSuchElementException();
			Entry<String,String> cur = data.data.get(index);
			index++;
			return cur;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("DataBoundProperties does not support the remove-during-iteration operation.");
		}

		
	}

	private void fireDataChangeNotification() {
		for(TableModelListener listener : listeners) listener.tableChanged(new TableModelEvent(this));
		for(BoundDataListener listener : boundDataListeners) listener.boundDataChanged(this);
	}
	
	@Override
	public void bind(BoundDataListener listener) {
		boundDataListeners.add(listener);
	}

	@Override
	public void unbind(BoundDataListener listener) {
		boundDataListeners.remove(listener);
	}

	@Override
	public void streamTo(OutputStream out) throws IOException {
		Streamable.Helper.intToStream(data.size(), out);
		for(SimpleEntry<String,String> entry : data) {
			Streamable.Helper.stringToStream(entry.getKey(), out);
			Streamable.Helper.stringToStream(entry.getValue(), out);
		}
	}

	@Override
	public void streamFrom(InputStream in) throws IOException {
		this.data.clear(); //Just in case we're called on an old object instead of a new one: wipe out the old data.
		int length = Streamable.Helper.intFromStream(in);
		if (length<=0) return;
		for(int i=0; i<length; i++) {
			String k = Streamable.Helper.stringFromStream(in);
			String v = Streamable.Helper.stringFromStream(in);
			put(k, v);
		}
	}
}
