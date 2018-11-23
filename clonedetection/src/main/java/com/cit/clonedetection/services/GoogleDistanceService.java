package com.cit.clonedetection.services;

import com.cit.clonedetection.model.Distance;
import com.cit.clonedetection.model.Location;
import com.cit.clonedetection.transfer.GoogleDistanceAPIResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Collections;

@Configuration
@Service
@Slf4j
public class GoogleDistanceService implements IDistanceService {
    private String apiKey;
    private RestTemplate restTemplate;

    @Autowired
    public GoogleDistanceService(RestTemplate restTemplate, String apiKey) {
        this.restTemplate=restTemplate;
        this.apiKey=apiKey;
    }

    public String getRequestURL(Location current, Location previous, Mode mode) {
        return getRequestURL(apiKey, current, previous, mode);
    }

    public String getRequestURL(String key, Location current, Location previous, Mode mode) {
        String baseUri = "https://maps.googleapis.com/maps/api/distancematrix/json?units=metric";
        String origin = String.format("%s,%s",current.getCoordinates().getLatitude(),current.getCoordinates().getLongitude());
        String destination = String.format("%s,%s",previous.getCoordinates().getLatitude(),previous.getCoordinates().getLongitude());
        return String.format("%s&origins=%s&destinations=%s&key=%s&mode=%s", baseUri,origin,destination, key, mode.toString());
    }


    public Distance execute(Location current, Location previous, Mode mode ) {

        Distance distance = Distance.builder()
                .distance(0)
                .duration(0)
                .mode(mode.toString())
                .status("ERROR")
                .build();

        if(apiKey==null)
            throw new IllegalArgumentException("GoogleDistanceService apiKey not set");


        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

        ResponseEntity<GoogleDistanceAPIResponseDTO> result = restTemplate.exchange(getRequestURL(apiKey,current,previous,mode), HttpMethod.GET, entity, GoogleDistanceAPIResponseDTO.class);
        if( result.getStatusCode().is2xxSuccessful() ) {

            GoogleDistanceAPIResponseDTO googleResult =  result.getBody();

            assert googleResult != null;

            String status = googleResult.getStatus();
            if(status.equals("REQUEST_DENIED"))
                throw new IllegalArgumentException("GoogleDistanceService REQUEST_DENIED : " + result.getBody() );

            int dis = googleResult.getRows().get(0).getElements().get(0).getDistance().getValue();
            int dur = googleResult.getRows().get(0).getElements().get(0).getDuration().getValue();

            distance = Distance.builder()
                    .distance(dis)
                    .duration(dur)
                    .mode(mode.toString())
                    .status("OK")
                    .build();

        }

        return distance;

    }

}



