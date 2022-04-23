package com.Coders_seem_to_be.FinalBestHack2.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="table1")
@Setter
@Getter
public class Station {
	@Id
	private String name;

	@Column
	private String address;

	@Column
	private String latitude;

	@Column
	private String longtitude;

	@Column
	private String country;

	@Column
	private String phone;

	@Column
	private String region;



}
