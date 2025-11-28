package uploader.service;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class HashUtilTest {

    @Test
    void testSha256() {
        HashUtil util = new HashUtil();
        String output = util.sha256("hello");
        assertThat(output)
                .isEqualTo("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824");
    }
}
