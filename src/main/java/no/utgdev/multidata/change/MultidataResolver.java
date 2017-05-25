package no.utgdev.multidata.change;

import io.vavr.Tuple2;

import java.util.List;

public interface MultidataResolver<ID, DOMAIN> {
    public int getBatchSize();
    public DOMAIN resolve(ChangeDescriptor<ID, DOMAIN> id);
    public List<Tuple2<ChangeDescriptor<ID, DOMAIN>, DOMAIN>> resolve(List<ChangeDescriptor<ID, DOMAIN>> ids);
}
