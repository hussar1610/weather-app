package pl.makselan.weatherapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.makselan.weatherapp.entity.SavedLocation;

import java.util.Optional;

@Repository
public interface SavedLocationRepository extends JpaRepository<SavedLocation, Long> {

    Optional<SavedLocation> findByName(String name);

}
