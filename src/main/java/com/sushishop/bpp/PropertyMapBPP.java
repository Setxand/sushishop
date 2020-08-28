package com.sushishop.bpp;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "property")
public class PropertyMapBPP implements BeanPostProcessor {

	private Map<String, Object> map = new HashMap<>();

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		Field[] fields = bean.getClass().getDeclaredFields();

		for (Field field : fields) {

			PropertyMap annotation = field.getAnnotation(PropertyMap.class);
			if (annotation != null) {
				if (field.getType() != Map.class)
					throw new IllegalArgumentException("Invalid annotated type: " + field.getType());

				HashMap<String, Object> mapToSet = new HashMap<>();

				String annotationValue = (String) map.get(annotation.value());
				String[] split = annotationValue.split(",");

				for (String s : split) {
					String[] prop = s.split("=");
					mapToSet.put(prop[0], Integer.valueOf(prop[1]));
				}

				try {
					field.setAccessible(true);
					field.set(bean, mapToSet);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}

		return bean;
	}

	public Map<String, Object> getMap() {
		return map;
	}

	public void setMap(Map<String, Object> map) {
		this.map = map;
	}
}
