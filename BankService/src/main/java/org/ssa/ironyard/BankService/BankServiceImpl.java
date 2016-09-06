package org.ssa.ironyard.BankService;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.ssa.ironyard.account.dao.AccountDAOEager;
import org.ssa.ironyard.account.model.Account;

public class BankServiceImpl implements BankService
{

    AccountDAOEager accountDAO;

    @Autowired
    public BankServiceImpl(AccountDAOEager accountDAO)
    {
        this.accountDAO = accountDAO;
    }

    @Override
    public Account withdraw(int account, BigDecimal amount) throws IllegalArgumentException
    {
        Account acc = accountDAO.read(account);
        if (acc != null)
        {

            BigDecimal newBalance = acc.getBalance().subtract(amount);

            if ((newBalance.compareTo(BigDecimal.ZERO) < 0))
            {
                throw new IllegalArgumentException("Insufficient Funds");
            }
            
            Account copy = acc.clone();
            copy.setBalance(newBalance);

            return accountDAO.update(copy);
        }

        throw new IllegalArgumentException("Account Not Found");
    }

    @Override
    public Account deposit(int account, BigDecimal amount)
    {
        Account acc = accountDAO.read(account);

        acc.setBalance(acc.getBalance().add(amount));

        return accountDAO.update(acc);
    }

    @Override
    public boolean transfer(int from, int to, BigDecimal amount) throws IllegalArgumentException
    {

        this.withdraw(from, amount);
        if(this.deposit(to, amount) != null)
            return true;
        
        throw new IllegalArgumentException("Account Not Found");

    }

}
