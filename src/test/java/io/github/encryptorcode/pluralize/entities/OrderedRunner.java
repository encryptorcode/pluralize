package io.github.encryptorcode.pluralize.entities;

import junitparams.JUnitParamsRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import java.util.ArrayList;
import java.util.List;

public class OrderedRunner extends JUnitParamsRunner {
    public OrderedRunner(Class<?> clazz) throws InitializationError {
        super(clazz);
    }

    @Override
    protected List<FrameworkMethod> computeTestMethods() {
        List<FrameworkMethod> list = super.computeTestMethods();
        List<FrameworkMethod> copy = new ArrayList<>(list);
        copy.sort((f1, f2) -> {
            Order o1 = f1.getAnnotation(Order.class);
            Order o2 = f2.getAnnotation(Order.class);

            if (o1 == null || o2 == null) {
                return -1;
            }
            return o1.value() - o2.value();
        });
        return copy;
    }
}
