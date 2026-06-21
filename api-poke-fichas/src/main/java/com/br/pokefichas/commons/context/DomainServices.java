package com.br.pokefichas.commons.context;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class DomainServices implements ApplicationContextAware {

    private static volatile ApplicationContext context;

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) {
        context = applicationContext;
    }

    public static <T> T get(final Class<T> clazz) {
        final ApplicationContext current = context;
        if (current == null) {
            throw new IllegalStateException(
                    "ApplicationContext nao inicializado. Em testes use @SpringBootTest, "
                            + "ou pule as validacoes de dominio com Builder.build(false)."
            );
        }
        return current.getBean(clazz);
    }

    public static <T> T get(final String name, final Class<T> clazz) {
        final ApplicationContext current = context;
        if (current == null) {
            throw new IllegalStateException(
                    "ApplicationContext nao inicializado. Em testes use @SpringBootTest, "
                            + "ou pule as validacoes de dominio com Builder.build(false)."
            );
        }
        return current.getBean(name, clazz);
    }
}
