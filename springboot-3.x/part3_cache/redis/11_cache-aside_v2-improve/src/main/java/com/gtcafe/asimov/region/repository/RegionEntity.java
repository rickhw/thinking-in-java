package com.gtcafe.asimov.region.repository;

import java.util.UUID;

import com.gtcafe.asimov.region.schema.RegionState;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "region")
@Data
public class RegionEntity {

    @Id
    @Column(name = "id")
	private String id;

	@Column(name = "region_code", unique = true)
	private String regionCode;

	@Column(name = "description")
	private String description;

	@Column(name = "state")
	private RegionState state;

	@Column(name = "creation_time")
	private long creationTime;

	public RegionEntity() {
		this.id = UUID.randomUUID().toString();
		this.state = RegionState.AVAILABLE;
	}

}
