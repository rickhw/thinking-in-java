package com.gtcafe.asimov;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.TreeMap;

@Component
public class BeanInspector {

    private final ApplicationContext applicationContext;

    public BeanInspector(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void listBeans() {
        // 確保 ApplicationContext 可以被轉型為 ConfigurableApplicationContext
        if (!(applicationContext instanceof ConfigurableApplicationContext)) {
            System.err.println("ApplicationContext does not support access to BeanFactory.");
            return;
        }

        ConfigurableApplicationContext configurableContext = (ConfigurableApplicationContext) applicationContext;

        // 獲取所有 Bean 名稱並按字母排序
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        Arrays.sort(beanNames);

        System.out.println("=== Spring Container Beans ===");

        for (String beanName : beanNames) {
            BeanDefinition beanDefinition = configurableContext.getBeanFactory().getBeanDefinition(beanName);

            // 收集屬性資訊
            String[] aliases = applicationContext.getAliases(beanName);
            String scope = beanDefinition.isSingleton() ? "singleton" : "prototype";
            String type = applicationContext.getType(beanName).getName();
            String resource = beanDefinition.getResourceDescription();
            String[] dependencies = beanDefinition.getDependsOn();

            // 建立輸出結構
            TreeMap<String, Object> beanInfo = new TreeMap<>();
            beanInfo.put("aliases", aliases);
            beanInfo.put("scope", scope);
            beanInfo.put("type", type);
            beanInfo.put("resource", resource);
            beanInfo.put("dependencies", dependencies != null ? dependencies : new String[0]);

            // 輸出結果
            // System.out.println(beanName + ": " + beanInfo.toString());
            System.out.println("  - [" + beanName + "]"
            );
        }
    }
}
