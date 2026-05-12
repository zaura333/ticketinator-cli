package com.ticketsystem.service;

import com.ticketsystem.model.Client;
import com.ticketsystem.repository.ClientRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Business logic for managing clients.
 */
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService() {
        this.clientRepository = new ClientRepository();
    }

    // ── Create ────────────────────────────────────────────────────────────────

    public Client createClient(String firstName, String lastName) {
        Client client = new Client(firstName, lastName);
        return clientRepository.save(client);
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    public Optional<Client> findById(UUID id) {
        return clientRepository.findById(id);
    }

    public Optional<Client> findById(String idString) {
        try {
            UUID uuid = UUID.fromString(idString.trim());
            return clientRepository.findById(uuid);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public List<Client> findAll() {
        return clientRepository.findAll();
    }

    public List<Client> searchByName(String query) {
        return clientRepository.searchByName(query);
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    public void deleteClient(Client client) {
        clientRepository.delete(client);
    }
}
