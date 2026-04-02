package com.accountia.business.feign;

import com.accountia.business.dto.ClientDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * FallbackFactory pour ClientFeignClient.
 *
 * POURQUOI FallbackFactory et pas Fallback directement ?
 * Avec @FeignClient(fallback = MaClasse.class), Spring crée 2 beans
 * du même type (l'interface Feign + la classe fallback), ce qui cause
 * "more than one bean of ClientFeignClient type".
 *
 * FallbackFactory résout ce problème car il est d'un type différent.
 */
@Component
public class ClientFeignClientFallbackFactory implements FallbackFactory<ClientFeignClient> {

    private static final Logger log = LoggerFactory.getLogger(ClientFeignClientFallbackFactory.class);

    @Override
    public ClientFeignClient create(Throwable cause) {
        log.error("Feign fallback déclenché pour client-ms. Raison: {}", cause.getMessage());

        return new ClientFeignClient() {

            @Override
            public ResponseEntity<List<ClientDTO>> getAllClients(String authorizationHeader) {
                log.warn("Fallback getAllClients - client-ms indisponible");
                return ResponseEntity.ok(Collections.emptyList());
            }

            @Override
            public ResponseEntity<ClientDTO> getClientById(Integer id, String authorizationHeader) {
                log.warn("Fallback getClientById({}) - client-ms indisponible", id);
                return ResponseEntity.ok(null);
            }

            @Override
            public ResponseEntity<List<ClientDTO>> searchClientsByNom(String nom, String authorizationHeader) {
                log.warn("Fallback searchClientsByNom({}) - client-ms indisponible", nom);
                return ResponseEntity.ok(Collections.emptyList());
            }

            @Override
            public ResponseEntity<List<ClientDTO>> searchClientsByNomEntreprise(String nomEntreprise, String authorizationHeader) {
                log.warn("Fallback searchClientsByNomEntreprise({}) - client-ms indisponible", nomEntreprise);
                return ResponseEntity.ok(Collections.emptyList());
            }
        };
    }
}