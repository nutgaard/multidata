package no.utgdev.multidata.source;

import java.util.List;

public interface MultidataSource<T> {
    public T save(T data);

    public List<T> save(List<T> data);
}
