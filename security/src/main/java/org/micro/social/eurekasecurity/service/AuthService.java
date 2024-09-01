package org.micro.social.eurekasecurity.service;


import org.micro.shareable.response.ResponseHandler;
import org.micro.social.eurekasecurity.dto.JwtRequest;
import org.micro.social.eurekasecurity.dto.JwtResponse;
import org.micro.social.eurekasecurity.dto.RegistrationUserDto;
import org.micro.shareable.dto.UserDto;
import org.micro.social.eurekasecurity.kafka.KafkaUserClient;
import org.micro.social.eurekasecurity.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.Set;

@Service
public class AuthService {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private KafkaUserClient kafkaUserClient;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RestTemplate restTemplate;

    public ResponseEntity<?> login(JwtRequest request) {
        UserDto userDto = kafkaUserClient.getUserByUsername(request.getUsername()).get();
        String username = userDto.getUsername();
        String requestName = request.getUsername();
        if (userDto != null & username.trim().equals(requestName.trim())) {
            String accessToken = jwtUtils.generate(userDto.getUsername(), userDto.getPassword(), userDto.getRoles(), "ACCESS");
            String refreshToken = jwtUtils.generate(userDto.getUsername(), userDto.getPassword(), userDto.getRoles(), "REFRESH");
            JwtResponse jwtResponse = new JwtResponse(accessToken, refreshToken);
            return ResponseHandler.responseBuilder(HttpStatus.OK, jwtResponse, "result");
        }
        return ResponseHandler.responseBuilder(HttpStatus.BAD_REQUEST, "Username or password is incorrect", "result");
    }

    public ResponseEntity<?> createNewUser(RegistrationUserDto newUserCandidate) {

        if (validatePassword(newUserCandidate)) {
            UserDto userDto = registrationUserToDto(newUserCandidate);
            Optional<String> result = kafkaUserClient.createUser(userDto);
            HttpStatus httpStatus;
            if (result.get().equals("Created")) {
                httpStatus = HttpStatus.CREATED;

                return ResponseHandler
                        .responseBuilder(httpStatus, result, "result");
            } else httpStatus = HttpStatus.BAD_REQUEST;
            return ResponseHandler
                    .responseBuilder(httpStatus, result.get(), "result");
        } else return ResponseHandler
                .responseBuilder(HttpStatus.BAD_REQUEST, "Password doesn't match", "result");
    }

    private Boolean validatePassword(RegistrationUserDto userDto) {
        return userDto.getPassword().equals(userDto.getConfirmPassword());
    }

    public UserDto registrationUserToDto(RegistrationUserDto registrationUserDto) {
        return new UserDto(
                registrationUserDto.getUsername(),
                //registrationUserDto.getPassword(),
                BCrypt.hashpw(registrationUserDto.getPassword(), BCrypt.gensalt()),
                registrationUserDto.getEmail(),
                registrationUserDto.getBirthDate(),
                Set.of(roleRepository.findByName("USER").get())
        );
    }
}


