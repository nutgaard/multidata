package no.utgdev.multidata.change;

import java.util.function.Function;

public interface ChangeDescriptor<ID, DOMAIN> extends Function<DOMAIN, DOMAIN> {
    public ID getid();
}
