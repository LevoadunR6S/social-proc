package org.micro.social.eurekaclient.repository;

import org.micro.social.eurekaclient.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Integer> {

    Optional<User> getByUsername(String username);
    Optional<User> getByPassword(String password);

    User getById(Integer id);
}
