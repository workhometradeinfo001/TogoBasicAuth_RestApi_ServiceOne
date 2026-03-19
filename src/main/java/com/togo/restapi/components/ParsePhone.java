package com.togo.restapi.components;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class ParsePhone {

    public Map<String, String> parsePhone(String fullPhone) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        Map<String, String> result = new HashMap<>();

        try {
            // fullPhone should be in E.164 format (e.g., +8801712345678)
            Phonenumber.PhoneNumber numberProto = phoneUtil.parse(fullPhone, "");

            String countryCode = "+" + numberProto.getCountryCode();
            String nationalNumber = String.valueOf(numberProto.getNationalNumber());

            result.put("countryCode", countryCode); // Result: +880
            result.put("number", nationalNumber);   // Result: 1712345678

        } catch (Exception e) {
            log.error("Unable to parse phone number: {}", fullPhone);
            // Fallback for your MongoDB index error
            result.put("countryCode", null);
            result.put("number", fullPhone);
        }
        return result;
    }
}
