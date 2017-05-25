package no.utgdev.multidata;

import java.util.List;
import java.util.function.Function;

public class Utils {
    public static <S, T> List<T> batchExec(int batchsize, Function<List<S>, List<T>> transform, List<S> data) {
        return io.vavr.collection.List.ofAll(data)
                .sliding(batchsize, batchsize)
                .flatMap(s -> transform.apply(s.asJava()))
                .toJavaList();
    }
}
