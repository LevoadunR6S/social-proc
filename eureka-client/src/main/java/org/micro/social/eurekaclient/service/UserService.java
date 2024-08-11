package org.micro.social.eurekaclient.service;


import org.micro.social.eurekaclient.repository.UserRepository;
import org.micro.social.eurekaclient.dto.RegistrationUserDto;
import org.micro.social.eurekaclient.dto.UserDto;
import org.micro.social.eurekaclient.model.User;
import org.micro.social.eurekaclient.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    private UserRepository userRepository;

    private RoleService roleService;

    private PasswordEncoder passwordEncoder;



    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    public void setRoleService(RoleService roleService) {
        this.roleService = roleService;
    }

    @Autowired
    public void setPasswordEncoder(PasswordEncoder encoder){this.passwordEncoder = encoder;}


    public User save(User user){
       return userRepository.save(user);
    }



    public List<User> getAll(){
        return userRepository.findAll();
    }

    public User getUserById(Integer id){
        return userRepository.getById(id);
    }


    public Optional<User> findByUsername(String username) {
        return userRepository.getByUsername(username);
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(
                String.format("User not found", username)
        ));
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.getName())).collect(Collectors.toList())
        );
    }

    public User createNewUser(RegistrationUserDto registrationUserDto) {
        User user = new User();
        user.setUsername(registrationUserDto.getUsername());
        user.setEmail(registrationUserDto.getEmail());
        //user.setPassword(passwordEncoder.encode(registrationUserDto.getPassword()));
        user.setBirthDate(registrationUserDto.getBirthDate());
        user.setRoles(List.of(roleService.getUserRole()));
        user.setPosts(new HashSet<>());
        user.setFriends(new HashSet<>());
        return save(user);
    }

    public UserDto userToUserDto(User user){
        return new UserDto(user.getId(), user.getUsername(),
                user.getEmail(), user.getBirthDate());
    }
}
