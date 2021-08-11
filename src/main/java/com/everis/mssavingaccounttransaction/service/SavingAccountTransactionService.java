package com.everis.mssavingaccounttransaction.service;

import com.everis.mssavingaccounttransaction.entity.SavingAccountTransaction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SavingAccountTransactionService {
    Mono<SavingAccountTransaction> create(SavingAccountTransaction t);

    Flux<SavingAccountTransaction> findAll();

    Mono<SavingAccountTransaction> findById(String id);

    Mono<SavingAccountTransaction> update(SavingAccountTransaction t);

    Mono<Boolean> delete(String t);

    Mono<Long> countMovements(String t);
}
