package eu.miaplatform.customplugin.springboot.controllers.Decorators;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import eu.miaplatform.customplugin.springboot.controllers.PostDecorator;
import eu.miaplatform.customplugin.springboot.models.EmailMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Base64;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = PostDecorator.class)
public class PostDecoratorTest {
    @Autowired
    private MockMvc mvc;
    private String jsonMockRequest;
    private String jsonMockResponse;
    private String wrongJsonMockResponse;

    public PostDecoratorTest() {
        byte[] signatureForPkey = Base64.getDecoder()
                .decode("MDwCHHvgCWrwNjuolo6uG5vVE+4wZqVnhILQ4tXF6G0CHGyJtuyL/F4FTmIntQCCCKhCS9m4l4N43PKn9js=");
        String pubKey = "MIIDQjCCAjUGByqGSM44BAEwggIoAoIBAQCPeTXZuarpv6vtiHrPSVG28y7FnjuvNxjo6sSWHz79Ngbn" +
                "Q1GpxBgzObgJ58KuHFObp0dbhdARrbi0eYd1SYRpXKwOjxSzNggooi/6JxEKPWKpk0U0CaD+aWxGWPhL3SCBnDcJoBBXsZWtzQA" +
                "jPbpUhLYpH51kjviDRIZ3l5zsBLQ0pqwudemYXeI9sCkvwRGMn/qdgYHnM423krcw17njSVkvaAmYchU5Feo9a4tGU8YzRY+AOz" +
                "KkwuDycpAlbk4/ijsIOKHEUOThjBopo33fXqFD3ktm/wSQPtXPFiPhWNSHxgjpfyEc2B3KI8tuOAdl+CLjQr5ITAV2OTlgHNZnA" +
                "h0AuvaWpoV499/e5/pnyXfHhe8ysjO65YDAvNVpXQKCAQAWplxYIEhQcE51AqOXVwQNNNo6NHjBVNTkpcAtJC7gT5bmHkvQkEq9" +
                "rI837rHgnzGC0jyQQ8tkL4gAQWDt+coJsyB2p5wypifyRz6Rh5uixOdEvSCBVEy1W4AsNo0fqD7UielOD6BojjJCilx4xHjGjQU" +
                "ntxyaOrsLC+EsRGiWOefTznTbEBplqiuH9kxoJts+xy9LVZmDS7TtsC98kOmkltOlXVNb6/xF1PYZ9j897buHOSXC8iTgdzEpba" +
                "iH7B5HSPh++1/et1SEMWsiMt7lU92vAhErDR8C2jCXMiT+J67ai51LKSLZuovjntnhA6Y8UoELxoi34u1DFuHvF9veA4IBBQACg" +
                "gEATn1Cm9DTgs6RYqWEnV8JEyweSSj4aParL8ldm84qtutLru2fva166riwDcqh3sWMyqJVNqzUF1ilVn8jAgtBQZ++VHF1RQ20" +
                "8ezkJYBle6yOee/z4bO7kmIZmwNQmraSrLBsciWUSfwrTPVmTrWs/3ZTdusTUF/HJdplykgMqAzS3vOHj+cYUSK5SqHVqbYwoeb" +
                "42R8k1vvF0Xfe+1GeSNUtqZghpeBTaeYxS/i1Lh66zK7IOpPZO8L04882+t1Dah88Ldj6evX+Q9qYW5wao1dhEuguRYcBatKbKD" +
                "0PJJNILVB7jJjHlybAbYjz8dPDKTJaNzMnHU9fAtu609uKgg==";
        EmailMessage mockRequestMessage = new EmailMessage();
        mockRequestMessage.setFrom("me");
        mockRequestMessage.setTo("you");
        mockRequestMessage.setObject("important matters");
        mockRequestMessage.setBody("meet me at 9");
        EmailMessage mockResponseMessage = mockRequestMessage.clone();
        mockResponseMessage.setEmailSignature(signatureForPkey);
        mockResponseMessage.setPubKey(pubKey);
        EmailMessage wrongMessage = mockResponseMessage.clone();
        wrongMessage.setBody("meet me at 7:30");
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        Gson gson = new Gson();
        jsonMockRequest = gson.toJson(mockRequestMessage, EmailMessage.class);
        jsonMockResponse = gson.toJson(mockResponseMessage, EmailMessage.class);
        wrongJsonMockResponse = gson.toJson(wrongMessage, EmailMessage.class);
    }

    @Test
    public void testAuthenticMessage() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                .post("/post/checksignature")
                .contentType("application/json")
                .characterEncoding("utf-8")
                .content("{\"request\":{\"body\":" + jsonMockRequest + "}," +
                        "\"response\":{\"body\":"+ jsonMockResponse + "}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headers.authentic-email").exists())
                .andExpect(jsonPath("$.headers.authentic-email").value("yes"));
    }

    @Test
    public void testNonAuthenticMessage() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                .post("/post/checksignature")
                .contentType("application/json")
                .characterEncoding("utf-8")
                .content("{\"request\":{\"body\":" + jsonMockRequest + "}," +
                        "\"response\":{\"body\":"+ wrongJsonMockResponse + "}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headers.authentic-email").exists())
                .andExpect(jsonPath("$.headers.authentic-email").value("no"));
    }
}
