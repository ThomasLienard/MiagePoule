package com.miage.pouleAPI.repositories;

import com.miage.pouleAPI.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Integer> {
    Optional<Place> findByNameAndStreetAndCity(String s, String street, String city);
}