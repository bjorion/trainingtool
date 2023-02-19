package org.jorion.trainingtool.dto.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.jorion.trainingtool.type.Role;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO {

    private Long id;

    private String userName;

    private String pnr;

    private Set<Role> roles = new HashSet<>();

    private String sector;

    private String function;

    private String lastName;

    private String firstName;

    private String phoneNumber;

    private String email;

    private String managerName;

    private boolean subContractor;

    private String createdOn;

    private String modifiedOn;
}
