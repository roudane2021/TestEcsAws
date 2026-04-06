package com.roudane.testecsaws;

import com.roudane.testecsaws.entity.Client;
import com.roudane.testecsaws.entity.Commande;
import com.roudane.testecsaws.repository.ClientRepository;
import com.roudane.testecsaws.repository.CommandeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private CommandeRepository commandeRepository;

    @Override
    public void run(String... args) throws Exception {
        // Création de clients de test
        Client client1 = new Client();
        client1.setNom("Dupont");
        client1.setPrenom("Jean");
        client1.setEmail("jean.dupont@example.com");

        Client client2 = new Client();
        client2.setNom("Martin");
        client2.setPrenom("Marie");
        client2.setEmail("marie.martin@example.com");

        Client client3 = new Client();
        client3.setNom("Dubois");
        client3.setPrenom("Pierre");
        client3.setEmail("pierre.dubois@example.com");

        clientRepository.save(client1);
        clientRepository.save(client2);
        clientRepository.save(client3);

        // Création de commandes de test
        Commande commande1 = new Commande();
        commande1.setDateCommande(LocalDate.now());
        commande1.setTotal(150.50);
        commande1.setClient(client1);

        Commande commande2 = new Commande();
        commande2.setDateCommande(LocalDate.now().minusDays(5));
        commande2.setTotal(75.25);
        commande2.setClient(client2);

        Commande commande3 = new Commande();
        commande3.setDateCommande(LocalDate.now().minusDays(10));
        commande3.setTotal(200.00);
        commande3.setClient(client1);

        commandeRepository.save(commande1);
        commandeRepository.save(commande2);
        commandeRepository.save(commande3);

        System.out.println("Données de test chargées avec succès !");
    }
}
