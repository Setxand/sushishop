package com.sushishop.dto;


import java.util.List;
import java.util.Set;

public class RecipeDTORequest {

	public String id;
	public String name;
	public String subName;
	public String picture;

	public List<String> productIds;

	public Set<String> keys;

}
