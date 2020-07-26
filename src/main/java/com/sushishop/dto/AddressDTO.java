package com.sushishop.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Set;

public class AddressDTO extends BaseDTO {

	public String id;
	public String city;
	public String street;
	public String house;
	public String entrance;
	public String housing;
	public String roomNumber;
	public String floor;

	@JsonIgnore
	public Set<String> keys;

	@Override
	public String toString() {
		return "\ncity: " + city +
				"\nstreet: " + street +
				"\nhouse: " + house +
				"\nentrance: " + entrance +
				"\nhousing: " + housing +
				"\nroomNumber: " + roomNumber +
				"\nfloor: " + floor;
	}
}
