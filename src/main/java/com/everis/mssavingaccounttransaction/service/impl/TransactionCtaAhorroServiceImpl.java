package com.everis.mssavingaccounttransaction.service.impl;

import com.everis.mssavingaccounttransaction.entity.SavingAccount;
import com.everis.mssavingaccounttransaction.entity.SavingAccountTransaction;
import com.everis.mssavingaccounttransaction.repository.SavingAccountTransactionRepository;
import com.everis.mssavingaccounttransaction.service.SavingAccountTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class TransactionCtaAhorroServiceImpl implements SavingAccountTransactionService {

    WebClient webClient = WebClient.create("http://localhost:8887/ms-saving-account/saving/savingAccount");

    @Autowired
    private SavingAccountTransactionRepository savingAccountTransactionRepository;

    @Override
    public Mono<SavingAccountTransaction> create(SavingAccountTransaction t) {
        return savingAccountTransactionRepository.save(t);
    }

    @Override
    public Flux<SavingAccountTransaction> findAll() {
        return savingAccountTransactionRepository.findAll();
    }

    @Override
    public Mono<SavingAccountTransaction> findById(String id) {
        return savingAccountTransactionRepository.findById(id);
    }

    @Override
    public Mono<SavingAccountTransaction> update(SavingAccountTransaction t) {
        return savingAccountTransactionRepository.save(t);
    }

    @Override
    public Mono<Boolean> delete(String t) {
        return savingAccountTransactionRepository.findById(t)
                .flatMap(tar -> savingAccountTransactionRepository.delete(tar).then(Mono.just(Boolean.TRUE)))
                .defaultIfEmpty(Boolean.FALSE);
    }

    @Override
    public Mono<Long> countMovements(String t) {
        return savingAccountTransactionRepository.findBySavingAccountId(t).count();
    }

    @Override
    public Mono<SavingAccount> findSavingAccountById(String id) {
        return webClient.get().uri("/find/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(SavingAccount.class);
    }

    @Override
    public Mono<SavingAccount> updateSavingAccount(SavingAccount sa) {
        return webClient.put().uri("/update", sa.getId())
                .accept(MediaType.APPLICATION_JSON)
                .syncBody(sa)
                .retrieve()
                .bodyToMono(SavingAccount.class);
    }


}
