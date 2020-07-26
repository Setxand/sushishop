package com.sushishop.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class User extends BaseModel {

	public enum UserRole {
		ROLE_USER
	}

	@Id
	@GenericGenerator(name = "uuid", strategy = "uuid")
	@GeneratedValue(generator = "uuid")
	private String id;
	private String name;

	@Column(unique = true)
	private String email;

	@Column(unique = true)
	private String phone;
	private String password;

	@OneToOne(cascade = CascadeType.ALL)
	public Address address;

	@OneToOne(cascade = CascadeType.ALL)
	private Cart cart;

	@Enumerated(EnumType.STRING)
	private UserRole role = UserRole.ROLE_USER;

}
