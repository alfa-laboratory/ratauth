package ru.ratauth.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class RestrictionUserKey implements Serializable {
    String userId;
    String acrValue;
    String clientId;
}
