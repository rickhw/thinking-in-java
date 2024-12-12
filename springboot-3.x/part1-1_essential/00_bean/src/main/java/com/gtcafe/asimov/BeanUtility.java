package com.gtcafe.asimov;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.stereotype.Component;

@Component
public class BeanUtility {

    private final ApplicationContext context;

    public BeanUtility(ApplicationContext context) {
        this.context = context;
    }

    // 列出所有 Singleton
    public void listSingletonBeans() {
        ConfigurableApplicationContext configurableContext = (ConfigurableApplicationContext) context;
        String[] beanNames = configurableContext.getBeanFactory().getBeanDefinitionNames();

        System.out.println("Singleton Beans:");
        for (String name : beanNames) {
            BeanDefinition beanDefinition = configurableContext.getBeanFactory().getBeanDefinition(name);
            if (beanDefinition.getScope().isEmpty() || "singleton".equals(beanDefinition.getScope())) {
                System.out.println("Bean name: " + name);
            }
        }
    }

    // 取得 Bean 實例
    public Object getBeanByName(String beanName) {
        if (context.containsBean(beanName)) {
            return context.getBean(beanName);
        } else {
            throw new IllegalArgumentException("Bean with name '" + beanName + "' not found in context.");
        }
    }
}
