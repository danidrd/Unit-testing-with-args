package Testable;
import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Specification {
    String[] argTypes () default {}; //
    String[] argValues () default {};
    String resType () default "";
    String resVal () default "";
}
