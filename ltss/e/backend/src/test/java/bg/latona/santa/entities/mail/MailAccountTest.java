package bg.latona.santa.entities.mail;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MailAccountTest {
    @Test
    public void passwordIsIgnored_whenSerialized_thenCorrect()
            throws JsonParseException, IOException {

        ObjectMapper mapper = new ObjectMapper();
        MailAccount entity = new MailAccount();

        String entityAsString = mapper.writeValueAsString(entity);
        assertThat(entityAsString, not(containsString("password")));
    }

    @Test
    void testSetPassword() {
        MailAccount entity = new MailAccount();
        entity.setPassword("testPass");
        assert ("testPass".equals(entity.getPassword()));
    }

    @Test
    void testSetUsername() {
        MailAccount entity = new MailAccount();
        entity.setUsername("testUsername");
        assert ("testUsername".equals(entity.getUsername()));
    }

    @Test
    public void testDeserialize2() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        MailAccount config = new MailAccount();
        config.setId(123L);
        config.setName("jack");
        config.setUsername("god");
        config.setPassword("cars");
        String json = mapper.writeValueAsString(config);
        assertThat(json, not(containsString("password")));
        assertThat(json, not(containsString("cars")));
        String original = "{\"id\":123,\"name\":\"jack\",\"username\":\"god\",\"password\":\"cars\"}";
        MailAccount des = mapper.readValue(original, MailAccount.class);
        assertThat(des.getPassword(), containsString("cars"));
    }
}