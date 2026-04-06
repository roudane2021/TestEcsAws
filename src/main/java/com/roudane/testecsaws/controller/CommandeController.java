package com.roudane.testecsaws.controller;

import com.roudane.testecsaws.entity.Commande;
import com.roudane.testecsaws.service.CommandeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/commandes")
public class CommandeController {

    @Autowired
    private CommandeService commandeService;

    @GetMapping
    public List<Commande> getAllCommandes() {
        return commandeService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Commande> getCommandeById(@PathVariable Long id) {
        return commandeService.findById(id)
                .map(commande -> ResponseEntity.ok(commande))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Commande createCommande(@RequestBody Commande commande) {
        return commandeService.save(commande);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Commande> updateCommande(@PathVariable Long id, @RequestBody Commande commandeDetails) {
        return commandeService.findById(id)
                .map(commande -> {
                    commande.setDateCommande(commandeDetails.getDateCommande());
                    commande.setTotal(commandeDetails.getTotal());
                    commande.setClient(commandeDetails.getClient());
                    Commande updatedCommande = commandeService.save(commande);
                    return ResponseEntity.ok(updatedCommande);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCommande(@PathVariable Long id) {
        if (commandeService.findById(id).isPresent()) {
            commandeService.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
