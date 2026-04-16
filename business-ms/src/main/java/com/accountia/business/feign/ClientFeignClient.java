package com.accountia.business.feign;

import com.accountia.business.dto.ClientDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Client OpenFeign pour communiquer avec le microservice client-ms.
 *
 * NOTE : On utilise fallbackFactory (pas fallback) pour éviter
 * le conflit "more than one bean of ClientFeignClient type".
 */
@FeignClient(
        name = "client-ms",
        fallbackFactory = ClientFeignClientFallbackFactory.class
)
public interface ClientFeignClient {

    @GetMapping("/api/client/clients")
    ResponseEntity<List<ClientDTO>> getAllClients(
            @RequestHeader("Authorization") String authorizationHeader
    );

    @GetMapping("/api/client/clients/{id}")
    ResponseEntity<ClientDTO> getClientById(
            @PathVariable("id") Integer id,
            @RequestHeader("Authorization") String authorizationHeader
    );

    @GetMapping("/api/client/clients/search")
    ResponseEntity<List<ClientDTO>> searchClientsByNom(
            @RequestParam("nom") String nom,
            @RequestHeader("Authorization") String authorizationHeader
    );

    @GetMapping("/api/client/clients/business")
    ResponseEntity<List<ClientDTO>> searchClientsByNomEntreprise(
            @RequestParam("nomEntreprise") String nomEntreprise,
            @RequestHeader("Authorization") String authorizationHeader
    );
}