package io.tabletoptools.hawthorne.modules.api;


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CachedMap implements Map<String, Object> {

    private final HashMap<String, CachedMapObject> map = new HashMap<>(32);

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        final Boolean[] found = {false};
        map.forEach((s, cachedMapObject) -> {
            if(cachedMapObject.getObject().equals(value)) {
                found[0] = true;
            }
        });
        return found[0];
    }

    @Override
    public Object get(Object key) {
        //TODO: Expire items here
        return map.get(key).getObject();
    }

    @Override
    public Object put(String key, Object value) {
        map.put(key, new CachedMapObject(value));
        return value;
    }

    @Override
    public Object remove(Object key) {
        return map.remove(key).getObject();
    }

    @Override
    public void putAll(Map m) {
        map.putAll(m);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Set<String> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<Object> values() {
        return map.values().stream().map(CachedMapObject::getObject).collect(Collectors.toList());
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return null;
    }
}
