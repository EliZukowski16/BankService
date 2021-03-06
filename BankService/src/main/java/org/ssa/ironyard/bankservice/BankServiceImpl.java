package org.ssa.ironyard.bankservice;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ssa.ironyard.account.model.Account;
import org.ssa.ironyard.dao.AbstractDAO;

@Service
public class BankServiceImpl implements BankService
{

    AbstractDAO<Account> accountDAO;

    public BankServiceImpl(AbstractDAO<Account> accountDAO)
    {
        this.accountDAO = accountDAO;
    }

    @Override
    @Transactional
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
    @Transactional
    public Account deposit(int account, BigDecimal amount)
    {
        Account acc = accountDAO.read(account);
        
        Account copy = acc.clone();

        copy.setBalance(acc.getBalance().add(amount));

        return accountDAO.update(copy);
    }

    @Override
    @Transactional
    public boolean transfer(int from, int to, BigDecimal amount) throws IllegalArgumentException
    {

        this.withdraw(from, amount);
        if(this.deposit(to, amount) != null)
            return true;
        
        throw new IllegalArgumentException("Account Not Found");

    }

}
