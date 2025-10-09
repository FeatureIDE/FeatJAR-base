package de.featjar.base.shell;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import de.featjar.base.FeatJAR;

public class ShellSession {
	
	private static class StoredElement<T> {
		private Class<T> type;
		private T element;
		
		public StoredElement(Class<T> type, T element) {
			this.type = type;
			this.element = element;
		}		
	}
	
	private final Map<String, StoredElement<?>> elements;	
	
	@SuppressWarnings("unchecked")
	public <T> Optional<T> get(String key, Class<T> type) {
		StoredElement<?> storedElement = elements.get(key);
		
		if(storedElement == null) {
			return Optional.empty();
		}		
		if (storedElement.type == type) {
			return Optional.of((T) storedElement.type.cast(storedElement.element));
		} else {
			throw new RuntimeException("Wrong Type"); // TODO Result<T> von Problem addProblem
		}
	}			
	
	public Optional<String> getType(String key) {
		return Optional.ofNullable(elements.get(key)).map(e -> e.type.getSimpleName());
	}
	
	public Optional<Object> getElement(String key) {
		return Optional.ofNullable(elements.get(key)).map(e -> e.element);
	}
	
	public ShellSession() {		
        elements = new LinkedHashMap<>();        
	}

	public <T> void put(String key, T element, Class<T> type) {
		elements.put(key, new StoredElement<T>(type, element));		
	}
	
	public Optional<?> remove(String key) {
		return Optional.ofNullable(elements.remove(key));		
	}
	
	public void clear() {
		elements.clear();
	}
	
	public int getSize() {
		return elements.size();
	}
	
	public boolean containsKey(String key) {
		return elements.containsKey(key);
	}
	
	public void printVariable(String key) {		
		for (Entry<String, StoredElement<?>> entry : elements.entrySet()) {
			if(entry.getKey().equals(key)) {
				FeatJAR.log().info("Variable: " + key + " Type: " + entry.getValue().type.getSimpleName() + "\n");
				break;
			}
		}
	}
	
	public void printVariables() {		
		elements.entrySet().forEach(m -> FeatJAR.log().info(m.getKey() + "   (" 
		+ m.getValue().type.getSimpleName() + ")" ));
	}
	
	public void printSortedByVarNames() {
		// TODO sort, group (type or name)
		
		elements.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(m -> FeatJAR.log().info(m.getKey() + " " + m.getValue().type.getSimpleName()));
	}
}
