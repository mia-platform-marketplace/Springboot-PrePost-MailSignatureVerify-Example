package eu.miaplatform.customplugin.springboot.controllers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import eu.miaplatform.customplugin.springboot.DecoratorUtils;
import eu.miaplatform.customplugin.springboot.models.EmailMessage;
import eu.miaplatform.customplugin.springboot.utils.SignatureUtils;
import eu.miaplatform.decorators.DecoratorResponse;
import eu.miaplatform.decorators.DecoratorResponseFactory;
import eu.miaplatform.decorators.predecorators.PreDecoratorRequest;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Base64;
import java.util.LinkedHashMap;

@RestController
@RequestMapping("/pre")
public class PreDecorator {
    @PostMapping("/signemail")
    @ApiOperation(value = "Apply DSA signature to an email before sending it")
    @ResponseBody
    public ResponseEntity<Serializable> signEmail(@RequestBody PreDecoratorRequest request) throws JsonProcessingException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        EmailMessage originalBody = getEmailFromRequest(request);
        if (Strings.isNullOrEmpty(originalBody.getFrom())) {
            try {
                originalBody.setFrom(request.getUserId());
            } catch (Exception ex) {
                DecoratorResponse response = DecoratorResponseFactory.abortChain(400);
                return DecoratorUtils.getResponseEntityFromDecoratorResponse(response);
            }
        }

        Base64.Encoder encoder = Base64.getEncoder();
        KeyPair keyPair = SignatureUtils.genKeyPair();
        byte[] encodedSignature = SignatureUtils.genSignature(originalBody.toString(), keyPair.getPrivate());
        originalBody.setEmailSignature(encodedSignature);
        originalBody.setPubKey(encoder.encodeToString(keyPair.getPublic().getEncoded()));

        PreDecoratorRequest updatedRequest = request.changeOriginalRequest().setBody(originalBody).build();
        DecoratorResponse response = DecoratorResponseFactory.makePreDecoratorResponse(updatedRequest);
        return DecoratorUtils.getResponseEntityFromDecoratorResponse(response);
    }

    private EmailMessage getEmailFromRequest(PreDecoratorRequest request) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        Gson gson = new Gson();
        String jsonRequestBody = gson.toJson(request.getOriginalRequestBody(), LinkedHashMap.class);
        return objectMapper.readValue(jsonRequestBody, EmailMessage.class);
    }
}
