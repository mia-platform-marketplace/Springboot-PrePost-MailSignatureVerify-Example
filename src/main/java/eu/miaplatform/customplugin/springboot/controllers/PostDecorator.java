package eu.miaplatform.customplugin.springboot.controllers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import eu.miaplatform.customplugin.springboot.DecoratorUtils;
import eu.miaplatform.customplugin.springboot.models.EmailMessage;
import eu.miaplatform.customplugin.springboot.utils.SignatureUtils;
import eu.miaplatform.decorators.DecoratorResponse;
import eu.miaplatform.decorators.DecoratorResponseFactory;
import eu.miaplatform.decorators.postdecorators.PostDecoratorRequest;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/post")
public class PostDecorator {
    @PostMapping("/checksignature")
    @ApiOperation(value = "Check if the email is authentic")
    @ResponseBody
    public ResponseEntity<Serializable> checkSignature(@RequestBody PostDecoratorRequest request) throws JsonProcessingException, InvalidKeySpecException, NoSuchAlgorithmException {
        EmailMessage emailResponseBody = getResponseEmailFromRequest(request);
        EmailMessage originalEmailBody = getOriginalEmailFromRequest(request);
        byte[] messageSignature = emailResponseBody.getEmailSignature();
        PublicKey pubKey = SignatureUtils.getPublicKey(emailResponseBody);
        boolean signatureVerified = false;
        Map<String, String> verifiedHeaders = new HashMap<>();
        try {
            signatureVerified = SignatureUtils.checkSignature(originalEmailBody.toString(), messageSignature, pubKey);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException ex) {
            ex.printStackTrace();
        }
        if (signatureVerified) {
            verifiedHeaders.put("authentic-email", "yes");
        } else {
            verifiedHeaders.put("authentic-email", "no");
        }
        PostDecoratorRequest updatedRequest = request.changeOriginalResponse().setHeaders(verifiedHeaders).build();
        DecoratorResponse decoratorResponse = DecoratorResponseFactory.makePostDecoratorResponse(updatedRequest);
        return DecoratorUtils.getResponseEntityFromDecoratorResponse(decoratorResponse);
    }

    private EmailMessage getResponseEmailFromRequest(PostDecoratorRequest request) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        Gson gson = new Gson();
        String jsonRequestBody = gson.toJson(request.getOriginalResponseBody(), LinkedHashMap.class);
        return objectMapper.readValue(jsonRequestBody, EmailMessage.class);
    }

    private EmailMessage getOriginalEmailFromRequest(PostDecoratorRequest request) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        Gson gson = new Gson();
        String jsonRequestBody = gson.toJson(request.getOriginalRequestBody(), LinkedHashMap.class);
        return objectMapper.readValue(jsonRequestBody, EmailMessage.class);
    }
}
