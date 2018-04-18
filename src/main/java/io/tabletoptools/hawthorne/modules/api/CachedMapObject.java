package io.tabletoptools.hawthorne.modules.api;

import java.util.Date;

public class CachedMapObject {
    private Object object;
    private Date lastAccessed;

    public CachedMapObject(Object object) {
        this.object = object;
        this.lastAccessed = new Date();
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public Date getLastAccessed() {
        return lastAccessed;
    }

    public void setLastAccessed(Date lastAccessed) {
        this.lastAccessed = lastAccessed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CachedMapObject)) return false;

        CachedMapObject that = (CachedMapObject) o;

        if (getObject() != null ? !getObject().equals(that.getObject()) : that.getObject() != null) return false;
        return getLastAccessed() != null ? getLastAccessed().equals(that.getLastAccessed()) : that.getLastAccessed() == null;
    }

    @Override
    public int hashCode() {
        return getObject() != null ? getObject().hashCode() : 0;
    }
}
