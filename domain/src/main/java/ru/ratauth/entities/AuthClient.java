package ru.ratauth.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author mgorelikov
 * @since 16/02/16
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthClient {
    /**
     * unique primary key
     */
    private String id;
    /**
     * should be human-readable unique sequence
     */
    private String name;
    private String description;
    private String password;
    private String salt;
    private Date created;
    /**
     * 256-bit sequence encoded in base64
     */
    private String secret;
    /**
     * in sec
     */
    private Date secretExpiresIn;
    private ClientType clientType;
    private Status status;

    public enum Status {
        ACTIVE,
        BLOCKED
    }
}
