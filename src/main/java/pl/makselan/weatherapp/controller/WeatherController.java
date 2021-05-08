package pl.makselan.weatherapp.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import pl.makselan.weatherapp.entity.AppUser;
import pl.makselan.weatherapp.entity.SavedLocation;
import pl.makselan.weatherapp.repository.AppUserRepository;
import pl.makselan.weatherapp.repository.SavedLocationRepository;
import pl.makselan.weatherapp.security.AppUserDetails;
import pl.makselan.weatherapp.weatherclient.RestWeatherClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController
@RequestMapping("/api/")
public class WeatherController {

    private final RestWeatherClient restWeatherClient;

    private final AppUserRepository appUserRepository;

    private final SavedLocationRepository savedLocationRepository;

    public WeatherController(RestWeatherClient restWeatherClient,
                             AppUserRepository appUserRepository,
                             SavedLocationRepository savedLocationRepository) {
        this.restWeatherClient = restWeatherClient;
        this.appUserRepository = appUserRepository;
        this.savedLocationRepository = savedLocationRepository;
    }

    @GetMapping("weather/{location}")
    public String getWeather(@PathVariable String location){
        return restWeatherClient.getWeather(location);
    }

    @GetMapping("locations/weather")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public String getWeatherForSavedLocations(){
        AppUser loggedAppUser = getLoggedAppUser();
        Set<SavedLocation> savedLocations = loggedAppUser.getSavedLocations();

        List<String> savedLocationsNames = new ArrayList<>();
        savedLocations
                .forEach(location -> {
                    savedLocationsNames.add(location.getName());
                });

        return restWeatherClient.getWeather(savedLocationsNames);
    }

    @PostMapping("locations/{location}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public String saveLocation(@PathVariable String location){
        if(!location.isBlank()){
            SavedLocation savedLocation = new SavedLocation(location);
            AppUser loggedAppUser = getLoggedAppUser();
            loggedAppUser.addSavedLocation(savedLocation);
            appUserRepository.save(loggedAppUser);
            return "Location has been saved!";
        } else{
            return "Error: Location query is empty!";
        }
    }

    @DeleteMapping("locations/{location}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public String deleteLocation(@PathVariable String location){
        AppUser loggedAppUser = getLoggedAppUser();
        Set<SavedLocation> savedLocations = loggedAppUser.getSavedLocations();
        SavedLocation savedLocation = savedLocations
                .stream()
                .filter((loc -> loc.getName().equals(location)))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Error - location " + location + " not found!"));

        loggedAppUser.removeSavedLocation(savedLocation);
        appUserRepository.save(loggedAppUser);
        return "Location has been removed!";
    }

    private AppUser getLoggedAppUser(){
        AppUserDetails appUserDetails = (AppUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return appUserRepository.findByUsername(appUserDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Error - user with username: " + appUserDetails.getUsername() + " not found!"));
    }

}
