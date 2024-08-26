package org.micro.social.eurekasecurity.dto;

import lombok.Data;

@Data
public class JwtRequest {
    private String username;
    private String password;
}
