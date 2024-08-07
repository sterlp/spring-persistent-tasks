package org.sterl.spring.persistent_tasks.example.bl.shared.model;

import jakarta.persistence.Transient;

public abstract class AbstractEntity<T> {

    public abstract T getId();
    
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (this == obj) return true;
        if (this.getId() == null) return false;
        
        if (this.getClass().isAssignableFrom(obj.getClass())
                && obj instanceof AbstractEntity objE) {
            return this.getId().equals(objE.getId());
        }
        return false;
    }
    
    @Transient
    private transient Integer hash; 

    @Override
    public int hashCode() {
        if (hash == null) {
            if (this.getId() == null) hash = super.hashCode();
            else hash = this.getId().hashCode();
        }
        return hash;
    }
    
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(id="+ getId() +")";
    }
}
