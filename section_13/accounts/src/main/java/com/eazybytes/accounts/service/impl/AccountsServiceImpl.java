package com.eazybytes.accounts.service.impl;

import com.eazybytes.accounts.constants.AccountsConstants;
import com.eazybytes.accounts.dto.AccountsDto;
import com.eazybytes.accounts.dto.AccountsMsgDto;
import com.eazybytes.accounts.dto.CustomerDto;
import com.eazybytes.accounts.entity.Accounts;
import com.eazybytes.accounts.entity.Customer;
import com.eazybytes.accounts.exception.CustomerAlreadyPresentException;
import com.eazybytes.accounts.exception.ResourceNotFoundException;
import com.eazybytes.accounts.mapper.AccountsMapper;
import com.eazybytes.accounts.mapper.CustomerMapper;
import com.eazybytes.accounts.repository.AccountsRepository;
import com.eazybytes.accounts.repository.CustomerRepository;
import com.eazybytes.accounts.service.IAccountsService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@AllArgsConstructor
public class AccountsServiceImpl implements IAccountsService {

    private static final Logger log = LoggerFactory.getLogger(AccountsServiceImpl.class);

    private CustomerRepository customerRepository;
    private AccountsRepository accountsRepository;
    private final StreamBridge streamBridge;
    @Override
    public void createAccount(CustomerDto customerDto) {
        Customer customer = CustomerMapper.mapToCustomer(customerDto, new Customer());
        Optional<Customer> optionalCustomer = customerRepository.findByMobileNumber(customerDto.getMobileNumber());
        if(optionalCustomer.isPresent()) {
            throw new CustomerAlreadyPresentException("Customer already registered with given mobileNumber "
                    +customerDto.getMobileNumber());
        }
        Customer savedCustomer = customerRepository.save(customer);
        Accounts savedAccount = accountsRepository.save(createNewAccount(savedCustomer));
        sendCommunication(savedAccount, savedCustomer);
    }

    private void sendCommunication(Accounts account, Customer customer) {
        var accountsMsgDto = new AccountsMsgDto(account.getAccountNumber(), customer.getName(),
                customer.getEmail(), customer.getMobileNumber());
        log.info("Sending Communication request for the details: {}", accountsMsgDto);
        var result = streamBridge.send("sendCommunication-out-0", accountsMsgDto);
        log.info("Is the Communication request successfully triggered ? : {}", result);
    }

    @Override
    public CustomerDto fetchAccount(String mobileNumber) {
        Customer customer = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
                ()->new ResourceNotFoundException("Customer","mobileNumber",mobileNumber)
        );

        Accounts accounts = accountsRepository.findByCustomerId(customer.getCustomerId()).orElseThrow(
                ()->new ResourceNotFoundException("Accounts","customerId",customer.getCustomerId().toString())
        );

        CustomerDto customerDto = CustomerMapper.mapToCustomerDto(customer, new CustomerDto());
        customerDto.setAccountsDto(AccountsMapper.mapToAccountsDto(accounts,new AccountsDto()));

        return  customerDto;


    }

    private Accounts createNewAccount(Customer customer){
        Accounts newAccounts = new Accounts();
        newAccounts.setCustomerId(customer.getCustomerId());
        long randomAccNumber = 1000000000L + new Random().nextInt(900000000);
        newAccounts.setAccountNumber(randomAccNumber);
        newAccounts.setAccountType(AccountsConstants.SAVINGS);
        newAccounts.setBranchAddress(AccountsConstants.ADDRESS);
        return newAccounts;
    }

    @Override
    public boolean updateAccount(CustomerDto customerDto) {
        boolean isUpdated = false;
        AccountsDto accountsDto = customerDto.getAccountsDto();
        if (accountsDto!=null){
            Accounts accounts = accountsRepository.findById(accountsDto.getAccountNumber()).orElseThrow(
                    ()-> new ResourceNotFoundException("Account","AccountNumber",accountsDto.getAccountNumber().toString())
            );
            AccountsMapper.mapToAccount(accountsDto,accounts);
            accounts = accountsRepository.save(accounts);


            Long customerId = accounts.getCustomerId();

            Customer customer = customerRepository.findById(customerId).orElseThrow(
                    ()->new ResourceNotFoundException("Customer","CustomerID",customerId.toString())
            );

            CustomerMapper.mapToCustomer(customerDto,customer);
            customerRepository.save(customer);
            isUpdated = true;


        }
        return isUpdated;
    }

    @Override
    public boolean deleteAccount(String mobielNumber) {

        Customer customer = customerRepository.findByMobileNumber(mobielNumber).orElseThrow(
                ()-> new ResourceNotFoundException("Customer","mobileNumber",mobielNumber)
        );

        accountsRepository.deleteByCustomerId(customer.getCustomerId());
        customerRepository.deleteById(customer.getCustomerId());
        return true;
    }

    @Override
    public boolean updateCommunicationStatus(Long accountNumber) {
        boolean isUpdated = false;
        if(accountNumber !=null ){
            Accounts accounts = accountsRepository.findById(accountNumber).orElseThrow(
                    () -> new ResourceNotFoundException("Account", "AccountNumber", accountNumber.toString())
            );
            accounts.setCommunicationSw(true);
            accountsRepository.save(accounts);
            isUpdated = true;
        }
        return  isUpdated;
    }
}
