package com.tsb.banking.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "accounts")
public class Account {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "customer_id", nullable = false)
  private Customer customer;

  @Column(nullable = false, unique = true)
  private String number;

  @Column(nullable = false, length = 3)
  private String currency;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal balance;

  @Column(nullable = false)
  private String status = "ACTIVE";

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Customer getCustomer() { return customer; }
  public void setCustomer(Customer customer) { this.customer = customer; }
  public String getNumber() { return number; }
  public void setNumber(String number) { this.number = number; }
  public String getCurrency() { return currency; }
  public void setCurrency(String currency) { this.currency = currency; }
  public BigDecimal getBalance() { return balance; }
  public void setBalance(BigDecimal balance) { this.balance = balance; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
}
