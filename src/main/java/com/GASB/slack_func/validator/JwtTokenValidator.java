package com.GASB.slack_func.validator;

//import com.GASB.slack_func.annotation.JwtToken;
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.security.Keys;
//import jakarta.validation.ConstraintValidator;
//import jakarta.validation.ConstraintValidatorContext;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import java.security.Key;
//
//@Component
//public class JwtTokenValidator implements ConstraintValidator<JwtToken, String> {
//
//    @Value("${jwt.secret}")
//    private String secretKey;
//
//    @Override
//    public boolean isValid(String token, ConstraintValidatorContext context) {
//        try {
//            Key key = Keys.hmacShaKeyFor(secretKey.getBytes());
//            Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
//            // Add additional claims validation if necessary
//            return true;
//        } catch (Exception e) {
//            return false;
//        }
//    }
//}