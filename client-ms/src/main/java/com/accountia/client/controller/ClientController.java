package com.accountia.client.controller;

import com.accountia.client.entity.Client;
import com.accountia.client.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/client")
@CrossOrigin(origins = "*")
public class ClientController {

    @Autowired
    private ClientService clientService;

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok("client-ms up");
    }

    // GET ALL - /api/client/clients
    @GetMapping("/clients")
    public ResponseEntity<List<Client>> getAllClients() {
        List<Client> clients = clientService.getAllClients();
        if (clients.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(clients);
    }

    // GET BY ID - /api/client/clients/1
    @GetMapping("/clients/{id}")
    public ResponseEntity<Client> getClientById(@PathVariable Integer id) {
        return clientService.getClientById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET BY NOM - /api/client/clients/search?nom=Ben
    @GetMapping("/clients/search")
    public ResponseEntity<List<Client>> searchByNom(@RequestParam String nom) {
        List<Client> clients = clientService.getClientsByNom(nom);
        if (clients.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(clients);
    }

    // POST - Créer un client
    @PostMapping(value = "/clients", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Client> createClient(@RequestBody Client client) {
        return new ResponseEntity<>(clientService.createClient(client), HttpStatus.CREATED);
    }

    // PUT - Modifier un client
    @PutMapping("/clients/{id}")
    public ResponseEntity<Client> updateClient(@PathVariable Integer id,
                                               @RequestBody Client client) {
        Client updated = clientService.updateClient(id, client);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(updated);
    }

    // DELETE - Supprimer un client
    @DeleteMapping("/clients/{id}")
    public ResponseEntity<String> deleteClient(@PathVariable Integer id) {
        return new ResponseEntity<>(clientService.deleteClient(id), HttpStatus.OK);
    }
}
