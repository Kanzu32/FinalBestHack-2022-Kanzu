package com.Coders_seem_to_be.FinalBestHack2.repository;

import com.Coders_seem_to_be.FinalBestHack2.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StationRepository extends JpaRepository<Station, String> {
	
}
