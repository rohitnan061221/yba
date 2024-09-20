package com.YourBank.utils;

import java.time.Year;

public class AccountUtils {

    public static final String ACCOUNT_EXISTS_CODE="001";
    public static final String ACCOUNT_EXISTS_MESSAGE="This User already has an account created.";
    public static final String ACCOUNT_CREATION_SUCCESS="002";
    public static final String ACCOUNT_CREATION_MESSAGE="Account has been successfully created.";
    public static final String ACCOUNT_NOT_EXIST_CODE="003";
    public static final String ACCOUNT_NOT_EXIST_MESSAGE="User with the provided account number does not exist.";
    public static final String ACCOUNT_FOUND_CODE="004";
    public static final String ACCOUNT_FOUND_SUCCESS="User Account Found.";
    public static final String AMOUNT_CREDITED_SUCCESS="005";
    public static final String AMOUNT_CREDITED_SUCCESS_MESSAGE="User Account is credited successfully.";
    public static final String INSUFFICIENT_BALANCE_CODE="006";
    public static final String INSUFFICIENT_BALANCE_MESSAGE="Insufficient Balance.";
    public static final String AMOUNT_DEBITED_SUCCESS="007";
    public static final String AMOUNT_DEBITED_SUCCESS_MESSAGE="Account has been successfully debited.";
    public static final String TRANSFER_SUCCESS_CODE="008";
    public static final String TRANSFER_SUCCESS_MESSAGE="Transfer successful.";



    public static String generateAccountNumber() {
        /**
         * current year+random 6digits
         */
        Year currentYear=Year.now();
        int min=100000;
        int max=999999;
        int randNumber= (int)Math.floor(Math.random()*(max-min+1)+min);

        String year=String.valueOf(currentYear);
        String randomNumber=String.valueOf(randNumber);
        StringBuilder accountNumber=new StringBuilder();
        return accountNumber.append(year).append(randomNumber).toString();
    }
}
