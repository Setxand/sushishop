package com.sushishop.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.MappedSuperclass;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
public class BaseModel {

	private static final String KIEV_ZONE_ID = "Europe/Kiev";

	private ZonedDateTime created = ZonedDateTime.now().withZoneSameInstant(ZoneId.of(KIEV_ZONE_ID));

}
