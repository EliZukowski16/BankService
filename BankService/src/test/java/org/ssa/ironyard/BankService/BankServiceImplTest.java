package org.ssa.ironyard.BankService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.ssa.ironyard.account.dao.AccountDAOEager;
import org.ssa.ironyard.account.model.Account;
import org.ssa.ironyard.account.model.Account.AccountType;
import org.ssa.ironyard.bankservice.BankService;
import org.ssa.ironyard.bankservice.BankServiceImpl;
import org.ssa.ironyard.customer.dao.CustomerDAOImpl;
import org.ssa.ironyard.customer.model.Customer;
import org.ssa.ironyard.dao.AbstractDAO;

import com.mysql.cj.jdbc.MysqlDataSource;

public class BankServiceImplTest
{

    static BankService bankService;

    static List<Customer> customersInDB;
    static List<Account> rawTestAccounts;
    static AbstractDAO<Account> accountDAO;
    static AbstractDAO<Customer> customerDAO;
    List<Account> accountsInDB;
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        String URL = "jdbc:mysql://localhost/ssa_bank?user=root&password=root&" +
        // "logger=org.ssa.ironyard.database.log.MySQLLog4jLogger&" +
                "useServerPrpStmts=true";

        MysqlDataSource mysqlDdataSource = new MysqlDataSource();
        mysqlDdataSource.setURL(URL);

        DataSource dataSource = mysqlDdataSource;

        accountDAO = new AccountDAOEager(dataSource);
        customerDAO = new CustomerDAOImpl(dataSource);

        customerDAO.clear();
        accountDAO.clear();

        customersInDB = new ArrayList<>();
        rawTestAccounts = new ArrayList<>();

        BufferedReader customerReader = null;
        BufferedReader accountReader = null;

        try
        {
            customerReader = Files.newBufferedReader(
                    Paths.get("C:\\Users\\admin\\workspace\\DatabaseApp\\resources\\MOCK_CUSTOMER_DATA.csv"),
                    Charset.defaultCharset());

            String line;

            while (null != (line = customerReader.readLine()))
            {
                String[] names = line.split(",");
                customersInDB.add(customerDAO.insert(new Customer(names[0], names[1])));
            }

            accountReader = Files.newBufferedReader(
                    Paths.get("C:\\Users\\admin\\workspace\\DatabaseApp\\resources\\MOCK_ACCOUNT_DATA.csv"),
                    Charset.defaultCharset());

            while (null != (line = accountReader.readLine()))
            {
                String[] accounts = line.split(",");
                rawTestAccounts.add(new Account(null, AccountType.getInstance(accounts[0]),
                        BigDecimal.valueOf(Double.parseDouble(accounts[1]))));
            }
        }
        catch (IOException iex)
        {
            System.err.println(iex);
            throw iex;
        }
        finally
        {
            if (null != customerReader)
                customerReader.close();
            if (null != accountReader)
                accountReader.close();
        }

        bankService = new BankServiceImpl((AccountDAOEager) accountDAO);

    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception
    {
        customerDAO.clear();
    }

    @Before
    public void setUp() throws Exception
    {
        accountDAO.clear();

        accountsInDB = new ArrayList<>();

    }

    @After
    public void tearDown() throws Exception
    {
        accountsInDB.clear();
        accountDAO.clear();
    }

    @Test
    public void bankServiceDepositTests()
    {
        Account testAccount = accountDAO
                .insert(new Account(customersInDB.get(0), AccountType.CHECKING, BigDecimal.valueOf(0.0)));

        Account updatedAccount = bankService.deposit(testAccount.getId(), BigDecimal.valueOf(0.0));

        assertTrue(testAccount.isLoaded());
        assertEquals(testAccount, updatedAccount);
        assertTrue(testAccount.deeplyEquals(updatedAccount));

        updatedAccount = bankService.deposit(testAccount.getId(), BigDecimal.valueOf(50.0));

        assertTrue(updatedAccount.isLoaded());
        assertEquals(testAccount, updatedAccount);
        assertEquals(0, updatedAccount.getBalance().compareTo(BigDecimal.valueOf(50.0)));
        assertFalse(testAccount.deeplyEquals(updatedAccount));

        Account updatedAccountFurther = bankService.deposit(testAccount.getId(), BigDecimal.valueOf(50.0));

        assertTrue(updatedAccountFurther.isLoaded());
        assertEquals(updatedAccount, updatedAccountFurther);
        assertFalse(updatedAccount.deeplyEquals(updatedAccountFurther));
        assertEquals(0, updatedAccountFurther.getBalance().compareTo(BigDecimal.valueOf(100.0)));
    }

    @Test
    public void bankServiceWithdrawalTests()
    {
        Account testAccount = accountDAO
                .insert(new Account(customersInDB.get(0), AccountType.CHECKING, BigDecimal.valueOf(50.0)));
        
        Account updatedAccount = bankService.withdraw(testAccount.getId(), BigDecimal.valueOf(0.0));
        
        assertTrue(testAccount.isLoaded());
        assertEquals(testAccount, updatedAccount);
        assertTrue(testAccount.deeplyEquals(updatedAccount));
        
        updatedAccount = bankService.withdraw(testAccount.getId(), BigDecimal.valueOf(25.0));
        
        assertTrue(updatedAccount.isLoaded());
        assertEquals(testAccount, updatedAccount);
        assertFalse(testAccount.deeplyEquals(updatedAccount));
        assertEquals(0, updatedAccount.getBalance().compareTo(BigDecimal.valueOf(25.0)));
        
        Account updatedAccountFurther = bankService.withdraw(testAccount.getId(), BigDecimal.valueOf(25.0));

        assertTrue(updatedAccountFurther.isLoaded());
        assertEquals(updatedAccount, updatedAccountFurther);
        assertFalse(updatedAccount.deeplyEquals(updatedAccountFurther));
        assertEquals(0, updatedAccountFurther.getBalance().compareTo(BigDecimal.valueOf(0.0)));
        
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Insufficient Funds");
        updatedAccount = bankService.withdraw(testAccount.getId(), BigDecimal.valueOf(50.0));
        
        updatedAccount = accountDAO.read(updatedAccount.getId());
        assertEquals(0, updatedAccount.getBalance().compareTo(BigDecimal.valueOf(0.0)));
        
        accountDAO.delete(testAccount.getId());
        
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Account Not Found");
        updatedAccount = bankService.withdraw(testAccount.getId(), BigDecimal.valueOf(0.0));
    }
    
    @Test
    public void bankServiceTransferTests()
    {
        Account testAccount1 = accountDAO
                .insert(new Account(customersInDB.get(0), AccountType.CHECKING, BigDecimal.valueOf(50.0)));
        
        Account testAccount2 = accountDAO
                .insert(new Account(customersInDB.get(1), AccountType.SAVINGS, BigDecimal.valueOf(50.0)));
        
        assertTrue(testAccount1.isLoaded());
        assertTrue(testAccount2.isLoaded());
        assertNotEquals(testAccount1, testAccount2);
        assertFalse(testAccount1.deeplyEquals(testAccount2));
        
        assertTrue(bankService.transfer(testAccount1.getId(), testAccount2.getId(), BigDecimal.valueOf(50.0)));
        assertTrue(bankService.transfer(testAccount2.getId(), testAccount1.getId(), BigDecimal.valueOf(100.0)));
        
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Insufficient Funds");
        bankService.transfer(testAccount2.getId(), testAccount1.getId(), BigDecimal.valueOf(1.0));
        
        customerDAO.delete(customersInDB.get(1).getId());
        
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Account Not Found");
        bankService.transfer(testAccount1.getId(), testAccount2.getId(), BigDecimal.valueOf(0.0));
        
        testAccount2 = accountDAO.insert(new Account(customersInDB.get(2), AccountType.CHECKING, BigDecimal.valueOf(100.0)));
        
        customerDAO.delete(customersInDB.get(0).getId());
        
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Account Not Found");
        bankService.transfer(testAccount1.getId(), testAccount2.getId(), BigDecimal.valueOf(0.0));
    }

}
