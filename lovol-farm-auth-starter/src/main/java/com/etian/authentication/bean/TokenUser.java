package com.etian.authentication.bean;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class TokenUser {

    private String userId;

    private String username;

    private String institutionId;

    private String saasId;


}
