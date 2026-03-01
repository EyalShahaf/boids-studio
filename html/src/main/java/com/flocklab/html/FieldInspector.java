package com.flocklab.html;

import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import java.lang.reflect.Field;

public class FieldInspector {
    public static void main(String[] args) {
        Class<?> clazz = GwtApplicationConfiguration.class;
        Field[] fields = clazz.getFields();
        for (Field field : fields) {
            System.out.println(field.getName());
        }
    }
}
