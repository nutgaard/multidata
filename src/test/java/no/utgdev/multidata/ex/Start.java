package no.utgdev.multidata.ex;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import no.utgdev.multidata.change.ChangeDescriptor;
import no.utgdev.multidata.change.MultidataResolver;
import no.utgdev.multidata.source.BatchingMultidataSource;
import no.utgdev.multidata.Multidata;
import no.utgdev.multidata.source.MultidataSource;
import no.utgdev.multidata.executor.TransactionManagerStrategy;

import javax.transaction.TransactionManager;
import java.util.LinkedHashMap;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public class Start {
    public static List<DomainClass> dataset = asList(
            new DomainClass("1", "name", 99, "apache"),
            new DomainClass("2", "name", 99, "apache"),
            new DomainClass("3", "name", 99, "apache"),
            new DomainClass("4", "name", 99, "apache"),
            new DomainClass("5", "name", 99, "apache"),
            new DomainClass("6", "name", 99, "apache"),
            new DomainClass("7", "name", 99, "apache"),
            new DomainClass("8", "name", 99, "apache"),
            new DomainClass("9", "name", 99, "apache"),
            new DomainClass("10", "old", 1337, "car")
    );

    public static void main(String[] args) {
        TransactionManager tm = new MyTransactionManager();
        Multidata<MyDatasources, DomainClass, String> multidata = new Multidata<>(
                tm,
                TransactionManagerStrategy.SINGLE,
                new LinkedHashMap<MyDatasources, MultidataSource<DomainClass>>() {{
//                    put(MyDatasources.SOURCE1, new Source1());
                    put(MyDatasources.SOURCE2, new BatchingMultidataSource<>(new Source2(), 3, tm));
                }}
        );

//        multidata.save(new DomainClass("0", "name", 99, "apache"));
//        multidata.save(dataset);

        multidata.save(new Resolver1(5), asList(
                new UppercaseDescriptor("1"),
                new UppercaseDescriptor("2"),
                new UppercaseDescriptor("4"),
                new UppercaseDescriptor("5"),
                new UppercaseDescriptor("7"),
                new UppercaseDescriptor("8"),
                new UppercaseDescriptor("9")
        ));
    }
}

enum MyDatasources {
    SOURCE1, SOURCE2
}

class UppercaseDescriptor implements ChangeDescriptor<String, DomainClass> {
    private String id;

    public UppercaseDescriptor(String id) {
        this.id = id;
    }

    @Override
    public String getid() {
        return this.id;
    }

    @Override
    public DomainClass apply(DomainClass domainClass) {
        return domainClass
                .withName(domainClass.name.toUpperCase());
    }
}

class Resolver1 implements MultidataResolver<String, DomainClass> {

    private int batchsize;

    public Resolver1(int batchsize) {
        this.batchsize = batchsize;
    }

    @Override
    public int getBatchSize() {
        return this.batchsize;
    }

    @Override
    public DomainClass resolve(ChangeDescriptor<String, DomainClass> desc) {
//        System.out.println("RESOLVED: 1");
        return Start.dataset.stream().filter((d) -> desc.getid().equals(d.id)).findFirst().get();
    }

    @Override
    public List<Tuple2<ChangeDescriptor<String, DomainClass>, DomainClass>> resolve(List<ChangeDescriptor<String, DomainClass>> descs) {
        System.out.println("RESOLVED: "+descs.size());
        return descs
                .stream()
                .map((desc) -> Tuple.of(desc, resolve(desc)))
                .collect(toList());
    }
}

class Source1 implements MultidataSource<DomainClass> {
    @Override
    public DomainClass save(DomainClass data) {
        System.out.println("[SOURCE 1] Single save: " + data);
        return data;
    }

    @Override
    public List<DomainClass> save(List<DomainClass> data) {
        System.out.println("[SOURCE 1] Batch save: " + data.size());
        return data;
    }
}

class Source2 implements MultidataSource<DomainClass> {
    @Override
    public DomainClass save(DomainClass data) {
        System.out.println("[SOURCE 2] Single save: " + data);
        return data;
    }

    @Override
    public List<DomainClass> save(List<DomainClass> data) {
        System.out.println("[SOURCE 2] Batch save: " + data.size());
        return data;
    }
}

class DomainClass {
    public String id;
    public String name;
    public int age;
    public String gender;

    public DomainClass(String id, String name, int age, String gender) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.gender = gender;
    }

    public DomainClass withName(String newName) {
        return new DomainClass(this.id, newName, this.age, this.gender);
    }

    @Override
    public String toString() {
        return "DomainClass{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", gender='" + gender + '\'' +
                '}';
    }
}