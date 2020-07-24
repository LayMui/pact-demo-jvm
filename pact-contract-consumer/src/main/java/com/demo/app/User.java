package com.demo.app;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.validation.annotation.Validated;


@Getter
@Setter
@ToString
@Validated
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {


    public User(long uuid, String username, String firstName, String lastName) {
        super();
        this.uuid = uuid;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
    }
    
    public User() {
	}

	private long uuid;  

    private String username;

    private String firstName;

    private String lastName;


}
