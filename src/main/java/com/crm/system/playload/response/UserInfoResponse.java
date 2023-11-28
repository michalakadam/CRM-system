package com.crm.system.playload.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
public class UserInfoResponse {
    private Long id;
    private String username;
    private String email;
    private List<String> roles;
    private int clientsNumber;

    public UserInfoResponse(Long id, String username, String email, List<String> roles, int clientsNumber) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
        this.clientsNumber = clientsNumber;
    }
}