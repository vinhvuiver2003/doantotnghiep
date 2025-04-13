package com.example.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MomoConfig {
    @Value("${momo.partner-code}")
    private String partnerCode;
    
    @Value("${momo.access-key}")
    private String accessKey;
    
    @Value("${momo.secret-key}")
    private String secretKey;
    
    @Value("${momo.endpoint}")
    private String endpoint;
    
    @Value("${momo.return-url}")
    private String returnUrl;
    
    @Value("${momo.notify-url}")
    private String notifyUrl;
    
    // Getters
    public String getPartnerCode() { 
        return partnerCode; 
    }
    
    public String getAccessKey() { 
        return accessKey; 
    }
    
    public String getSecretKey() { 
        return secretKey; 
    }
    
    public String getEndpoint() { 
        return endpoint; 
    }
    
    public String getReturnUrl() { 
        return returnUrl; 
    }
    
    public String getNotifyUrl() { 
        return notifyUrl; 
    }
} 