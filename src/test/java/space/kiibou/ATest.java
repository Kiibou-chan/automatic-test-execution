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

}
