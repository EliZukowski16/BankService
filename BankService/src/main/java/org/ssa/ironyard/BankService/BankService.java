package org.ssa.ironyard.BankService;


import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ssa.ironyard.account.model.Account;


@Service
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
    

    @Transactional
    Account withdraw(int account, BigDecimal amount) throws IllegalArgumentException;

    /**
     *
     * @param account
     * @param amount
     * @return a loaded Account with its balance reflecting the deposit
     */
    
    @Transactional
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
    
    @Transactional
    boolean transfer(int from, int to, BigDecimal amount) throws IllegalArgumentException;

}
