package io.github.glaforge.gemini.interactions;
import io.github.glaforge.ansiren.Ansi;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class FindStaticMethods {
    public static void main(String[] args) {
        for (Method m : Ansi.class.getMethods()) {
            if (Modifier.isStatic(m.getModifiers())) {
                System.out.println(m.getName() + "(" + Arrays.toString(m.getParameterTypes()) + ") -> " + m.getReturnType().getSimpleName());
            }
        }
    }
}
