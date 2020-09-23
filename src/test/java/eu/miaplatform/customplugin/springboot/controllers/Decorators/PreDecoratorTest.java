package eu.miaplatform.customplugin.springboot.controllers.Decorators;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import eu.miaplatform.customplugin.springboot.controllers.PreDecorator;
import eu.miaplatform.customplugin.springboot.models.EmailMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = PreDecorator.class)
public class PreDecoratorTest {
    @Autowired
    private MockMvc mvc;

    @Test
    public void getSignatureTest() throws Exception {
        EmailMessage mockMessage = new EmailMessage();
        mockMessage.setFrom("me");
        mockMessage.setTo("you");
        mockMessage.setObject("important matters");
        mockMessage.setBody("meet me at 9");
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        Gson gson = new Gson();
        String jsonRequestBody = gson.toJson(mockMessage, EmailMessage.class);

        mvc.perform(MockMvcRequestBuilders
           .post("/pre/signemail")
           .contentType("application/json")
           .characterEncoding("utf-8")
           .content("{\"body\":" + jsonRequestBody + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body.pubKey").exists())
                .andExpect(jsonPath("$.body.pubKey").isNotEmpty())
                .andExpect(jsonPath("$.body.emailSignature").exists())
                .andExpect(jsonPath("$.body.emailSignature").isNotEmpty());
    }
}
