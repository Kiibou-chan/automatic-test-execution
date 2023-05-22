package space.kiibou;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ATest {

    @Test
    public void testIdentity() {
        ClassUnderTest obj = new ClassUnderTest();

        Assertions.assertEquals("abc", obj.identity("abc"));
    }

    @Test
    public void testWrongIdentity() {
        ClassUnderTest obj = new ClassUnderTest();

        Assertions.assertEquals("abc", obj.wrongIdentity("abc"));
    }

    @Test
    public void testCondFunNeg() {
        ClassUnderTest obj = new ClassUnderTest();

        Assertions.assertEquals("Negative", obj.conditionalFun(-1));
    }

    @Test
    public void testCondFunSmall() {
        ClassUnderTest obj = new ClassUnderTest();

        Assertions.assertEquals("Small", obj.conditionalFun(5));
    }

    @Test
    public void testCondFunBig() {
        ClassUnderTest obj = new ClassUnderTest();

        Assertions.assertEquals("Big", obj.conditionalFun(1337));
    }

}
