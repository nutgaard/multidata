package no.utgdev.multidata;

import no.utgdev.multidata.change.ChangeDescriptor;
import no.utgdev.multidata.change.MultidataResolver;
import no.utgdev.multidata.executor.MultidataExecutor;
import no.utgdev.multidata.executor.TransactionManagerStrategy;
import no.utgdev.multidata.source.MultidataSource;

import javax.transaction.TransactionManager;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class Multidata<SOURCES, DOMAIN, ID> implements MultidataSource<DOMAIN> {
    private final Map<SOURCES, MultidataSource<DOMAIN>> sourcesConfig;
    private final MultidataExecutor<DOMAIN> executor;

    public Multidata(Map<SOURCES, MultidataSource<DOMAIN>> sourcesConfig) {
        this(null, TransactionManagerStrategy.SINGLE, sourcesConfig);
    }

    public Multidata(TransactionManager transactionManager, Map<SOURCES, MultidataSource<DOMAIN>> sourcesConfig) {
        this(transactionManager, TransactionManagerStrategy.ALL, sourcesConfig);
    }

    public Multidata(TransactionManager transactionManager, TransactionManagerStrategy transactionManagerStrategy, Map<SOURCES, MultidataSource<DOMAIN>> sourcesConfig) {
        this.sourcesConfig = sourcesConfig;
        this.executor = new MultidataExecutor<>(transactionManager, transactionManagerStrategy);
    }

    public DOMAIN save(MultidataResolver<ID, DOMAIN> resolver, ChangeDescriptor<ID, DOMAIN> changeDescriptor, SOURCES... toSources) {
        DOMAIN resolved = resolver.resolve(changeDescriptor);
        DOMAIN domain = changeDescriptor.apply(resolved);
        return save(domain, toSources);
    }

    public DOMAIN save(MultidataResolver<ID, DOMAIN> resolver, ChangeDescriptor<ID, DOMAIN> changeDescriptor) {
        return save(resolver, changeDescriptor, allSources());
    }

    public List<DOMAIN> save(MultidataResolver<ID, DOMAIN> resolver, List<ChangeDescriptor<ID, DOMAIN>> allChangeDescriptors, SOURCES... toSources) {
        return Utils.batchExec(
                resolver.getBatchSize(),
                (changeDescriptors) -> {
                    List<DOMAIN> domain = resolver
                            .resolve(changeDescriptors)
                            .stream()
                            .map(t -> t._1.apply(t._2))
                            .collect(toList());
                    return save(domain, toSources);
                },
                allChangeDescriptors
        );
    }

    public List<DOMAIN> save(MultidataResolver<ID, DOMAIN> resolver, List<ChangeDescriptor<ID, DOMAIN>> allChangeDescriptors) {
        return save(resolver, allChangeDescriptors, allSources());
    }

    public DOMAIN save(DOMAIN data) {
        return save(data, allSources());
    }

    public List<DOMAIN> save(List<DOMAIN> data) {
        return save(data, allSources());
    }

    public DOMAIN save(DOMAIN data, SOURCES... toSources) {
        List<Callable> sourceJobs = Stream.of(toSources)
                .map(sourcesConfig::get)
                .map((source) -> (Callable) () -> source.save(data))
                .collect(toList());

        return (DOMAIN) this.executor.execute(sourceJobs);
    }

    public List<DOMAIN> save(List<DOMAIN> data, SOURCES... toSources) {
        List<Callable> sourceJobs = Stream.of(toSources)
                .map(sourcesConfig::get)
                .map((source) -> (Callable) () -> source.save(data))
                .collect(toList());

        return (List<DOMAIN>) this.executor.execute(sourceJobs);
    }

    private SOURCES[] allSources() {
        return (SOURCES[]) sourcesConfig.keySet().toArray();
    }
}
