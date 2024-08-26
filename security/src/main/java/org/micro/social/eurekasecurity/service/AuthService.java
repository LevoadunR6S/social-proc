package org.micro.social.eurekasecurity.service;


import org.micro.shareable.response.ResponseHandler;
import org.micro.social.eurekasecurity.dto.RegistrationUserDto;
import org.micro.shareable.dto.UserDto;
import org.micro.social.eurekasecurity.kafka.KafkaUserClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AuthService implements UserDetailsService {


    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private KafkaUserClient kafkaUserClient;


    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserDto> user = kafkaUserClient.getUserByUsername(username);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        return userDtoToUser(user.get());
    }


    public ResponseEntity<?> createNewUser(RegistrationUserDto newUserCandidate) {
        if (validatePassword(newUserCandidate)) {
            UserDto userDto = registrationUserToDto(newUserCandidate);
            Optional<String> result = kafkaUserClient.createUser(userDto);
            HttpStatus httpStatus;
            if (result.get().equals("Created")){
                httpStatus=HttpStatus.CREATED;
            }
            else httpStatus = HttpStatus.BAD_REQUEST;

            return ResponseHandler
                    .responseBuilder(httpStatus, result , "result");
        } else return ResponseHandler
                .responseBuilder(HttpStatus.BAD_REQUEST, "Password doesn't match", "result");
    }

    private Boolean validatePassword(RegistrationUserDto userDto) {
        return userDto.getPassword().equals(userDto.getConfirmPassword());
    }

    public UserDto registrationUserToDto(RegistrationUserDto registrationUserDto) {
        return new UserDto(
                registrationUserDto.getUsername(),
                registrationUserDto.getPassword(),
                registrationUserDto.getEmail(),
                registrationUserDto.getBirthDate(),
                null);
    }

    public User userDtoToUser(UserDto userDto) {
        return new org.springframework.security.core.userdetails.User(
                userDto.getUsername(),
                userDto.getPassword(),
                userDto.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getName())).collect(Collectors.toSet())
        );
    }

}
