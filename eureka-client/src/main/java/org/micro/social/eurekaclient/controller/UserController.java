package org.micro.social.eurekaclient.controller;


import org.micro.social.eurekaclient.model.User;
import org.micro.shareable.response.ResponseHandler;
import org.micro.social.eurekaclient.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {


    @Autowired
    private UserService userService;

    @PostMapping("/auth")
    public ResponseEntity<?> createUser(@RequestBody User user){
        User createdUser = userService.createNewUser(user);
        return ResponseHandler.responseBuilder(HttpStatus.CREATED,createdUser,"user");
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Integer id){
        User user = userService.getUserById(id);
        if (user!=null){
            return ResponseHandler.responseBuilder(HttpStatus.OK,user,"created user");
        }
        else {
            return ResponseHandler.responseBuilder(HttpStatus.NOT_FOUND,null,"user not found");
        }
    }


}
