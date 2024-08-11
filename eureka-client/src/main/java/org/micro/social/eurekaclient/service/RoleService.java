package org.micro.social.eurekaclient.service;

import lombok.RequiredArgsConstructor;
import org.micro.social.eurekaclient.model.Role;
import org.micro.social.eurekaclient.repository.RoleRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;

    public Role getUserRole() {
        return roleRepository.findByName("USER").get();  //без перевірки що така роль справді існує
    }
}
