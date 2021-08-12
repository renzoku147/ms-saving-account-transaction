package com.everis.mssavingaccounttransaction.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SavingAccount {

    String id;

    private Customer customer;

    private String cardNumber;

    private List<Person> holders;

    private List<Person> signers;

    private Integer limitTransactions;

    private Integer freeTransactions;

    private Double commissionTransactions;

    private Double balance;

    private Double minAverageVip;

    private LocalDateTime date;
}
