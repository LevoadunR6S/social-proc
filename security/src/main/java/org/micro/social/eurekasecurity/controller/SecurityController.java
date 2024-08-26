package org.micro.social.eurekasecurity.controller;


import org.micro.shareable.response.ResponseHandler;
import org.micro.social.eurekasecurity.dto.RegistrationUserDto;
import org.micro.social.eurekasecurity.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class SecurityController {

    @Autowired
    private AuthService authService;

    @GetMapping("/signup")
    public ResponseEntity<?> createNewUser(){
        return ResponseHandler.responseBuilder(HttpStatus.OK,"Hi","response");
    }

    @PostMapping("/signup")
    public ResponseEntity<?> createNewUser(@RequestBody RegistrationUserDto userDto){
        return authService.createNewUser(userDto);
    }


    /*@PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody JwtRequest request){
    }*/


}
