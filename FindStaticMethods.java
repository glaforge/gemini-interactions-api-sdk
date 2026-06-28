import io.github.glaforge.ansiren.Ansi;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class FindStaticMethods {
    public static void main(String[] args) {
        for (Method m : Ansi.class.getMethods()) {
            if (Modifier.isStatic(m.getModifiers())) {
                System.out.println(m.getName() + " -> " + m.getReturnType().getSimpleName());
            }
        }
    }
}
