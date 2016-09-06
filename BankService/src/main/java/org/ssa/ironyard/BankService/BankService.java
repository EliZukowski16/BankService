package org.ssa.ironyard.BankService;


import java.math.BigDecimal;

import org.ssa.ironyard.account.model.Account;



public interface BankService
{

    /**
     *
     * @param account
     * @param amount
     * @return a loaded Account with its balance reflecting the withdrawal
     * @throws IllegalArgumentException
     *             if the withdrawal is deemed invalid
     */
    

 
    Account withdraw(int account, BigDecimal amount) throws IllegalArgumentException;

    /**
     *
     * @param account
     * @param amount
     * @return a loaded Account with its balance reflecting the deposit
     */
    

    Account deposit(int account, BigDecimal amount);

    /**
     *
     * @param from
     *            the account to transfer from
     * @param to
     *            the account to transfer to
     * @param amount
     * @return whether the transfer was successful
     * @throws IllegalArgumentException
     *             if the transfer is deemed invalid
     */
    

    boolean transfer(int from, int to, BigDecimal amount) throws IllegalArgumentException;

}
