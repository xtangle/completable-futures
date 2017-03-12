import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class FutureTest {

    class Image {
        private String name;

        Image(String name) {
            this.name = name;
        }

        String getName() {
            return name;
        }

        void setName(String name) {
            this.name = name;
        }

        void load() throws InterruptedException, IllegalArgumentException {
            Thread.sleep((long) (Math.random() * 100) + 25 * name.charAt(0));
            System.out.println(String.format("Loaded image: %s", this.name));
        }
    }

    private final List<Image> IMAGES = Arrays.asList(
            new Image("Cat"),
            new Image("Dog"),
            new Image("Elephant"),
            new Image("Fox")
    );

    @Test
    public void testSerialLoad() throws Exception {
        for (Image image : IMAGES) {
            image.load();
        }
    }

    @Test
    public void testGet() {

    }

    @Test
    public void testCancel() {

    }

    @Test
    public void testGet_WithTimeout() {

    }

}
