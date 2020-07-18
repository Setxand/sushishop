package com.sushishop.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Address {

	@Id
	private String id;
	private String city;
	private String street;
	private String house;
	private String entrance;
	private String housing;
	private String roomNumber;
	private String floor;

	public Address(String id) {
		this.id = id;
	}
}
