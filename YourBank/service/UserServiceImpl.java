package com.YourBank.service;

import com.YourBank.dto.*;
import com.YourBank.entity.User;
import com.YourBank.repository.UserRepository;
import com.YourBank.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;

@Service
public class UserServiceImpl implements UserService{

    @Autowired
    UserRepository userRepository;

    @Autowired
    EmailService emailService;
    private Object isAccountExist;

    @Override
    public BankResponse createAccount(UserRequest userRequest) {
        /**
         * creating an account-saving new user info in db
         * check if account number exsists
         */

        if(userRepository.existsByEmail(userRequest.getEmail())){
            return BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_EXISTS_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_EXISTS_MESSAGE)
                    .accountInfo(null)
                    .build();
        }
        User newUser= User.builder()
                .firstname(userRequest.getFirstname())
                .lastname(userRequest.getLastname())
                .otherName(userRequest.getOtherName())
                .gender(userRequest.getGender())
                .address(userRequest.getAddress())
                .stateofOrigin(userRequest.getStateofOrigin())
                .accountNumber(AccountUtils.generateAccountNumber())
                .accountBalance(BigDecimal.ZERO)
                .email(userRequest.getEmail())
                .phoneNumber(userRequest.getPhoneNumber())
                .alternativePhoneNumber(userRequest.getAlternativePhoneNumber())
                .status("ACTIVE")
                .build();

        User savedUser= userRepository.save(newUser);
        // Send Email alert
        EmailDetails emailDetails= EmailDetails.builder()
                .recipient(savedUser.getEmail())
                .subject("ACCOUNT CREATION")
                .messageBody("Congratulations your account has been created.\nYour Account Details: \n"+
                        "Account Name :"+savedUser.getFirstname()+" "+savedUser.getLastname()+" "+savedUser.getOtherName()+"\n Account Number :"+savedUser.getAccountNumber())
                .build();
        emailService.sendEmailAlert(emailDetails);

        return BankResponse.builder()
                .responseCode(AccountUtils.ACCOUNT_CREATION_SUCCESS)
                .responseMessage(AccountUtils.ACCOUNT_CREATION_MESSAGE)
                .accountInfo(AccountInfo.builder()
                        .accountBalance(savedUser.getAccountBalance())
                        .accountNumber(savedUser.getAccountNumber())
                        .accountName(savedUser.getFirstname()+" "+savedUser.getLastname()+" "+savedUser.getOtherName())
                        .build())
                .build();


    }

    @Override
    public BankResponse balanceEnquiry(EnquiryRequest request) {
    Boolean isAccountExist=userRepository.existsByAccountNumber(request.getAccountNumber());
    if (!isAccountExist){
        return BankResponse.builder()
                .responseCode(AccountUtils.ACCOUNT_NOT_EXIST_CODE)
                .responseMessage(AccountUtils.ACCOUNT_NOT_EXIST_MESSAGE)
                .accountInfo(null)
                .build();
    }

    User foundUser=userRepository.findByAccountNumber(request.getAccountNumber());
    return BankResponse.builder()
            .responseCode(AccountUtils.ACCOUNT_FOUND_CODE)
            .responseMessage(AccountUtils.ACCOUNT_FOUND_SUCCESS)
            .accountInfo(AccountInfo.builder()
                    .accountBalance(foundUser.getAccountBalance())
                    .accountNumber(foundUser.getAccountNumber())
                    .accountName(foundUser.getFirstname()+" "+foundUser.getLastname()+" "+foundUser.getOtherName())
                    .build())
            .build();
    }

    @Override
    public String nameEnquiry(EnquiryRequest request) {
        Boolean isAccountExist=userRepository.existsByAccountNumber(request.getAccountNumber());
        if (!isAccountExist){
            return AccountUtils.ACCOUNT_NOT_EXIST_MESSAGE;
        }
        User foundUser=userRepository.findByAccountNumber(request.getAccountNumber());
        return foundUser.getFirstname()+" "+foundUser.getLastname()+" "+foundUser.getOtherName();
    }

    @Override
    public BankResponse creditAccount(CreditDebitRequest request) {
        Boolean isAccountExist=userRepository.existsByAccountNumber(request.getAccountNumber());
        if (!isAccountExist){
            return BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_NOT_EXIST_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_NOT_EXIST_MESSAGE)
                    .accountInfo(null)
                    .build();
        }
        User userToCredit=userRepository.findByAccountNumber(request.getAccountNumber());
        userToCredit.setAccountBalance(userToCredit.getAccountBalance().add(request.getAmount()));
        userRepository.save(userToCredit);
        return BankResponse.builder()
                .responseCode(AccountUtils.AMOUNT_CREDITED_SUCCESS)
                .responseMessage(AccountUtils.AMOUNT_CREDITED_SUCCESS_MESSAGE)
                .accountInfo(AccountInfo.builder()
                        .accountName(userToCredit.getFirstname()+" "+userToCredit.getLastname()+" "+userToCredit.getOtherName())
                        .accountBalance(userToCredit.getAccountBalance())
                        .accountNumber(request.getAccountNumber())
                        .build())
                .build();
    }

    @Override
    public BankResponse debitAccount(CreditDebitRequest request) {
        Boolean isAccountExist=userRepository.existsByAccountNumber(request.getAccountNumber());
        if (!isAccountExist){
            return BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_NOT_EXIST_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_NOT_EXIST_MESSAGE)
                    .accountInfo(null)
                    .build();
        }
        User userToDebit=userRepository.findByAccountNumber(request.getAccountNumber());
        BigInteger availableBalance=userToDebit.getAccountBalance().toBigInteger();
        BigInteger debitAmount=request.getAmount().toBigInteger();
        if(availableBalance.intValue()<debitAmount.intValue()){
            return BankResponse.builder()
                    .responseCode(AccountUtils.INSUFFICIENT_BALANCE_CODE)
                    .responseMessage(AccountUtils.INSUFFICIENT_BALANCE_MESSAGE)
                    .accountInfo(null)
                    .build();
        }
        else{
            userToDebit.setAccountBalance(userToDebit.getAccountBalance().subtract(request.getAmount()));
            userRepository.save(userToDebit);
            return BankResponse.builder()
                    .responseCode(AccountUtils.AMOUNT_DEBITED_SUCCESS)
                    .responseMessage(AccountUtils.AMOUNT_DEBITED_SUCCESS_MESSAGE)
                    .accountInfo(AccountInfo.builder()
                            .accountName(userToDebit.getFirstname()+" "+userToDebit.getLastname()+" "+userToDebit.getOtherName())
                            .accountBalance(userToDebit.getAccountBalance())
                            .accountNumber(request.getAccountNumber())
                            .build())
                    .build();
        }
    }

    @Override
    public BankResponse transfer(TransferRequest request) {

        boolean isDestinationAccountExist= userRepository.existsByAccountNumber(request.getDestinationAccountNumber());
        if (!isDestinationAccountExist){
            return BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_NOT_EXIST_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_NOT_EXIST_MESSAGE)
                    .accountInfo(null)
                    .build();
        }
        User sourceAccountUser=userRepository.findByAccountNumber(request.getSourceAccountNumber());
        if(request.getAmount().compareTo(sourceAccountUser.getAccountBalance())>0){
            return BankResponse.builder()
                    .responseCode(AccountUtils.INSUFFICIENT_BALANCE_CODE)
                    .responseMessage(AccountUtils.INSUFFICIENT_BALANCE_MESSAGE)
                    .accountInfo(null)
                    .build();
        }
        sourceAccountUser.setAccountBalance(sourceAccountUser.getAccountBalance().subtract(request.getAmount()));
        String sourceUsername= sourceAccountUser.getFirstname()+" "+sourceAccountUser.getLastname()+" "+sourceAccountUser.getOtherName();
        userRepository.save(sourceAccountUser);
        EmailDetails debitAlert=EmailDetails.builder()
                .subject("DEBIT ALERT")
                .recipient(sourceAccountUser.getEmail())
                .messageBody("The sum of"+request.getAmount()+"has been deducted from your account. Your current account balance is"+sourceAccountUser.getAccountBalance())
                .build();

            emailService.sendEmailAlert(debitAlert);

        User destinationAccountUser=userRepository.findByAccountNumber(request.getDestinationAccountNumber());
        destinationAccountUser.setAccountBalance(destinationAccountUser.getAccountBalance().add(request.getAmount()));
        userRepository.save(destinationAccountUser);
       // String recipientUsername=destinationAccountUser.getFirstname()+" "+destinationAccountUser.getLastname()+" "+destinationAccountUser.getOtherName();
        EmailDetails creditAlert=EmailDetails.builder()
                .subject("CREDIT ALERT")
                .recipient(sourceAccountUser.getEmail())
                .messageBody("The sum of "+request.getAmount()+" has been sent to your account from "+sourceUsername+". Your current account balance is"+sourceAccountUser.getAccountBalance())
                .build();

        emailService.sendEmailAlert(creditAlert);
        return BankResponse.builder()
                .responseCode(AccountUtils.TRANSFER_SUCCESS_CODE)
                .responseMessage(AccountUtils.TRANSFER_SUCCESS_MESSAGE)
                .accountInfo(null)
                .build();

    }
}
