package com.everis.mssavingaccounttransaction.controller;

import com.everis.mssavingaccounttransaction.entity.SavingAccount;
import com.everis.mssavingaccounttransaction.entity.SavingAccountTransaction;
import com.everis.mssavingaccounttransaction.service.SavingAccountTransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/transactionSavingAccount")
@Slf4j
public class TransactionSavingAccountController {
    @Autowired
    SavingAccountTransactionService savingAccountTransactionService;

    WebClient webClient = WebClient.create("http://localhost:8021/savingAccount");

    @GetMapping("list")
    public Flux<SavingAccountTransaction> findAll(){
        return savingAccountTransactionService.findAll();
    }

    @GetMapping("/find/{id}")
    public Mono<SavingAccountTransaction> findById(@PathVariable String id){
        return savingAccountTransactionService.findById(id);
    }

    @PostMapping("/create")
    public Mono<ResponseEntity<SavingAccountTransaction>> create(@RequestBody SavingAccountTransaction savingAccountTransaction){
       Mono<SavingAccount> savingAccount = webClient.get().uri("/find/{id}", savingAccountTransaction.getSavingAccount().getId())
                                            .accept(MediaType.APPLICATION_JSON)
                                            .retrieve()
                                            .bodyToMono(SavingAccount.class); // Limite Movimientos

       return savingAccountTransactionService.countMovements(savingAccountTransaction.getSavingAccount().getId()) // N° Movimientos actuales
               .flatMap(cnt -> {
                   return savingAccount
                           .filter(sa -> sa.getLimitMovements() > cnt)
                           .flatMap(sa -> {
                               switch (savingAccountTransaction.getTypeTransaction()){
                                   case DEPOSIT: sa.setBalance(sa.getBalance() + savingAccountTransaction.getTransactionAmount());
                                                return webClient.put().uri("/update", sa.getId())
                                                   .accept(MediaType.APPLICATION_JSON)
                                                   .syncBody(sa)
                                                   .retrieve()
                                                   .bodyToMono(SavingAccount.class);
                                   case DRAFT: sa.setBalance(sa.getBalance() - savingAccountTransaction.getTransactionAmount());
                                               return webClient.put().uri("/update", sa.getId())
                                                       .accept(MediaType.APPLICATION_JSON)
                                                       .syncBody(sa)
                                                       .retrieve()
                                                       .bodyToMono(SavingAccount.class);
                                   default: return Mono.empty();
                               }
                           })
                           .flatMap(sa -> {
                               savingAccountTransaction.setSavingAccount(sa);
                               savingAccountTransaction.setTransactionDateTime(LocalDateTime.now());
                               return savingAccountTransactionService.create(savingAccountTransaction);
                           })
                           .map(sat ->new ResponseEntity<>(sat , HttpStatus.CREATED) );
               })
               .defaultIfEmpty(new ResponseEntity<>(HttpStatus.BAD_REQUEST));





//            return savingAccountTransactionService.create(c)
//                    .map(savedCustomer -> new ResponseEntity<>(savedCustomer , HttpStatus.CREATED));
    }

    @PutMapping("/update")
    public Mono<ResponseEntity<SavingAccountTransaction>> update(@RequestBody SavingAccountTransaction transaction) {
        Mono<SavingAccount> savingAccount = webClient.get().uri("/find/{id}", transaction.getSavingAccount().getId())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(SavingAccount.class); // Limite Movimientos

        return savingAccount
                .flatMap(sa -> {
                    return savingAccountTransactionService.findById(transaction.getId())
                            .flatMap(sat -> {
                                switch (transaction.getTypeTransaction()){
                                    case DEPOSIT: sa.setBalance(sa.getBalance() - sat.getTransactionAmount() + transaction.getTransactionAmount());
                                        return webClient.put().uri("/update", sa.getId())
                                                .accept(MediaType.APPLICATION_JSON)
                                                .syncBody(sa)
                                                .retrieve()
                                                .bodyToMono(SavingAccount.class).flatMap(saUpdate -> {
                                                                                            transaction.setSavingAccount(saUpdate);
                                                                                            transaction.setTransactionDateTime(LocalDateTime.now());
                                                                                            return savingAccountTransactionService.update(transaction);
                                                                                        });
                                    case DRAFT: sa.setBalance(sa.getBalance() + sat.getTransactionAmount() - transaction.getTransactionAmount());
                                        return webClient.put().uri("/update", sa.getId())
                                                .accept(MediaType.APPLICATION_JSON)
                                                .syncBody(sa)
                                                .retrieve()
                                                .bodyToMono(SavingAccount.class).flatMap(saUpdate -> {
                                                                                                transaction.setSavingAccount(saUpdate);
                                                                                                transaction.setTransactionDateTime(LocalDateTime.now());
                                                                                                return savingAccountTransactionService.update(transaction);
                                                                                            });
                                    default: return Mono.empty();
                                } // Mono<SavingAccount>
                            });
                })
                .map(sat ->new ResponseEntity<>(sat , HttpStatus.CREATED))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

    }

    @DeleteMapping("/delete/{id}")
    public Mono<ResponseEntity<String>> delete(@PathVariable String id) {
        return savingAccountTransactionService.delete(id)
                .filter(deleteCustomer -> deleteCustomer)
                .map(deleteCustomer -> new ResponseEntity<>("Customer Deleted", HttpStatus.ACCEPTED))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
