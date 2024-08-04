package com.example.userservice.services;

import com.example.userservice.models.Token;
import com.example.userservice.models.User;
import com.example.userservice.repos.TokenRepository;
import com.example.userservice.repos.UserRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import java.util.random.RandomGenerator;

@Service
public class UserService {

    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private UserRepository userRepository;
    private TokenRepository tokenRepository;

    public UserService(BCryptPasswordEncoder bCryptPasswordEncoder, UserRepository userRepository,
                       TokenRepository tokenRepository) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
    }

    public User createUser(String email, String password, String name) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setHashedPassword(bCryptPasswordEncoder.encode(password));

        return userRepository.save(user);
    }

    public User validateToken(String tokenValue) {
        Optional<Token> token = tokenRepository.findByTokenValueAndExpiryAtGreaterThan(tokenValue, new Date());
        if(token.isEmpty()) {
            throw new BadCredentialsException("Invalid token");
        }

        return token.get().getUser();
    }

    public Token login(String email, String password) {
        //1. Verify if the user exists
        Optional<User> user = userRepository.findByEmail(email);
        if(user.isEmpty()) {
            throw new UsernameNotFoundException("User not found for email: " + email);
        }

        //2. Verify the password
        if(!bCryptPasswordEncoder.matches(password, user.get().getHashedPassword())) {
            throw new BadCredentialsException("Bad credentials");
        }

        //3. Generate Token
        Token token = generateToke(user.get());
        tokenRepository.save(token);

        return token;
    }

    private Token generateToke(User user) {
        Token token = new Token();
        token.setUser(user);
        token.setTokenValue(RandomStringUtils.randomAlphanumeric(15));
        LocalDate currentDate = LocalDate.now();
        LocalDate thirtyDaysLater = currentDate.plusDays(30);
        Date expiryDate = Date.from(thirtyDaysLater.atStartOfDay(ZoneId.systemDefault()).toInstant());
        token.setExpiryAt(expiryDate);
        return token;
    }
}
