package com.accountia.expense.dto;

import com.accountia.expense.entity.Expense;

public class ExpenseDetailsDTO {

    private Expense expense;
    private BusinessDTO business;
    private ClientDTO client;

    public ExpenseDetailsDTO() {}

    public ExpenseDetailsDTO(Expense expense, BusinessDTO business, ClientDTO client) {
        this.expense = expense;
        this.business = business;
        this.client = client;
    }

    public Expense getExpense() { return expense; }
    public void setExpense(Expense expense) { this.expense = expense; }

    public BusinessDTO getBusiness() { return business; }
    public void setBusiness(BusinessDTO business) { this.business = business; }

    public ClientDTO getClient() { return client; }
    public void setClient(ClientDTO client) { this.client = client; }
}