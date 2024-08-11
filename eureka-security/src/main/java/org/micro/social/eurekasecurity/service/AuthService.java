package org.micro.social.eurekasecurity.service;

import lombok.RequiredArgsConstructor;
import org.micro.social.eurekaclient.dto.RegistrationUserDto;
import org.micro.social.eurekaclient.dto.UserDto;
import org.micro.social.eurekaclient.exception.InvalidUserException;
import org.micro.social.eurekaclient.model.User;
import org.micro.social.eurekaclient.response.ResponseHandler;
import org.micro.social.eurekaclient.service.UserService;
import org.micro.social.eurekasecurity.dto.JwtRequest;
import org.micro.social.eurekasecurity.dto.JwtResponse;
import org.micro.social.eurekasecurity.utils.JwtUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final JwtUtils jwtTokenUtils;
    private final AuthenticationManager authenticationManager;

    public ResponseEntity<?> createAuthToken(@RequestBody JwtRequest authRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
        } catch (BadCredentialsException e) {
            return ResponseHandler.responseBuilder(HttpStatus.UNAUTHORIZED,
                    new BadCredentialsException("Incorrect login or password"),"error cause");
        }
        UserDetails userDetails = userService.loadUserByUsername(authRequest.getUsername());
        String token = jwtTokenUtils.generateToken(userDetails);
        return ResponseHandler.responseBuilder(HttpStatus.CREATED,new JwtResponse(token),"token");
    }

    public ResponseEntity<?> createNewUser(@RequestBody RegistrationUserDto registrationUserDto) {
        if (!registrationUserDto.getPassword().equals(registrationUserDto.getConfirmPassword())) {
            return ResponseHandler.responseBuilder(HttpStatus.BAD_REQUEST, new BadCredentialsException("Password not match"),"error cause");
        }
        if (userService.findByUsername(registrationUserDto.getUsername()).isPresent()) {
            return ResponseHandler.responseBuilder(HttpStatus.BAD_REQUEST, new InvalidUserException("User already exists"),"error cause");
        }
        User user = userService.createNewUser(registrationUserDto);
        return ResponseHandler.responseBuilder(HttpStatus.CREATED,
                new UserDto(user.getId(), user.getUsername(), user.getEmail(),user.getBirthDate()), "user");
    }
}
