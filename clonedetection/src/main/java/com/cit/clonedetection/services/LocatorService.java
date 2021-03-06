package com.cit.clonedetection.services;

import com.cit.clonedetection.exceptions.PanelNotFoundException;
import com.cit.clonedetection.model.Location;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Slf4j
@Service
public class LocatorService implements ILocatorService{

    @Value("${uri.location.service.panels:http://localhost:8080/api/locations}")
    private String uri;

    @Autowired
    private RestTemplate restTemplate;

    // store of panel locations
    private Map<UUID, Location> panelLocations = new HashMap<>();




    private void populateCacheFromRemotePanelService()
    {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
            ResponseEntity<Map<UUID, Location>> result = restTemplate.exchange(uri, HttpMethod.GET, entity, new ParameterizedTypeReference<Map<UUID, Location>>() {
            });

            if (result.getStatusCode().is2xxSuccessful()) {
                panelLocations.clear();
                panelLocations.putAll(result.getBody());
            }

        } catch (Exception exception) {
                log.error("Unable to connect to remote panel location service : {}", exception);
        }
    }


    private boolean isCacheEmpty() {
        return panelLocations.isEmpty();
    }


    @Override
    public Location getLocationFromPanelId(String panelId) {

        // fill up the cache if its empty as we need something to search
        if (isCacheEmpty()) {
            populateCacheFromRemotePanelService();
        }

        // find the panel in our panel locations cache
        Location location = this.panelLocations.get( UUID.fromString(panelId) );
        if (location==null) {
            if (log.isErrorEnabled()) {
                log.error("Validation request  failed, Can not find this panelId={}", panelId );
            }
            throw new PanelNotFoundException("Panel not found Id=" + panelId);
        }
        return location;
    }
}
