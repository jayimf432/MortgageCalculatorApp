package com.mortgageapp;

import java.util.Scanner;
import java.text.NumberFormat;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

// Core Mortgage Calculator Class
class MortgageCalculator {

    private final static byte MONTHS_IN_YEAR = 12;
    private final static byte PERCENT = 100;

    private int principal;
    private float annualInterest;
    private byte years;

    public MortgageCalculator(int principal, float annualInterest, byte years) {
        this.principal = principal;
        this.annualInterest = annualInterest;
        this.years = years;
    }

    public double calculateBalance(short numberOfPaymentsMade) {
        float monthlyInterest = getMonthlyInterest();
        float numberOfPayments = getNumberOfPayments();

        double balance = principal
                * (Math.pow(1 + monthlyInterest, numberOfPayments) - Math.pow(1 + monthlyInterest, numberOfPaymentsMade))
                / (Math.pow(1 + monthlyInterest, numberOfPayments) - 1);

        return balance;
    }

    public double calculateMortgage() {
        float monthlyInterest = getMonthlyInterest();
        float numberOfPayments = getNumberOfPayments();

        double mortgage = principal
                * (monthlyInterest * Math.pow(1 + monthlyInterest, numberOfPayments))
                / (Math.pow(1 + monthlyInterest, numberOfPayments) - 1);

        return mortgage;
    }

    public double[] getRemainingBalances() {
        double[] balances = new double[getNumberOfPayments()];
        for (short month = 1; month <= balances.length; month++) {
            balances[month - 1] = calculateBalance(month);
        }
        return balances;
    }

    private float getMonthlyInterest() {
        return annualInterest / PERCENT / MONTHS_IN_YEAR;
    }

    private int getNumberOfPayments() {
        return years * MONTHS_IN_YEAR;
    }
}

// Report Class for Displaying Results
class MortgageReport {

    private final NumberFormat currency;
    private MortgageCalculator calculator;

    public MortgageReport(MortgageCalculator calculator) {
        this.calculator = calculator;
        currency = NumberFormat.getCurrencyInstance();
    }

    public void printMortgage() {
        double mortgage = calculator.calculateMortgage();
        System.out.println();
        System.out.println("MORTGAGE");
        System.out.println("--------");
        System.out.println("Monthly Payments: " + currency.format(mortgage));
    }

    public void printPaymentSchedule() {
        System.out.println();
        System.out.println("PAYMENT SCHEDULE");
        System.out.println("----------------");

        for (double balance : calculator.getRemainingBalances()) {
            System.out.println(currency.format(balance));
        }
    }

    public void saveReportToFile(String fileName) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write("--- Mortgage Calculator Report ---\n\n");
            writer.write("Date: " + LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")) + "\n\n");
            writer.write("MORTGAGE\n--------\n");
            writer.write("Monthly Payments: " + currency.format(calculator.calculateMortgage()) + "\n\n");

            writer.write("PAYMENT SCHEDULE\n----------------\n");
            for (double balance : calculator.getRemainingBalances()) {
                writer.write(currency.format(balance) + "\n");
            }

            System.out.println("\nReport saved to " + fileName + "\n");
        }
    }
}

// Utility Class for Input Handling
class InputReader {
    private static Scanner scanner = new Scanner(System.in);

    public static double readNumber(String prompt, double min, double max) {
        double value;
        while (true) {
            System.out.print(prompt);
            value = scanner.nextDouble();
            if (value >= min && value <= max)
                break;
            System.out.println("Enter a value between " + min + " and " + max);
        }
        return value;
    }

    public static String readString(String prompt) {
        System.out.print(prompt);
        return scanner.next();
    }
}

// Main Class
public class Main {
    public static void main(String[] args) throws IOException {

        System.out.println("--- Welcome to the Mortgage Calculator App ---");
        int principal = (int) InputReader.readNumber("Principal ($1k - $1M): ", 1_000, 1_000_000);
        float annualInterest = (float) InputReader.readNumber("Annual Interest Rate (1-30%): ", 1, 30);
        byte years = (byte) InputReader.readNumber("Period (Years, 1-30): ", 1, 30);

        var calculator = new MortgageCalculator(principal, annualInterest, years);
        var report = new MortgageReport(calculator);

        // Print reports
        report.printMortgage();
        report.printPaymentSchedule();

        // Save report to file
        String fileName = "Mortgage_Report_" + LocalDate.now() + ".txt";
        report.saveReportToFile(fileName);

        // Interactive Features
        String response = InputReader.readString("\nWould you like to calculate another mortgage? (yes/no): ").toLowerCase();
        if (response.equals("yes")) {
            main(args); // Restart the application
        } else {
            System.out.println("Thank you for using the Mortgage Calculator App. Goodbye!");
        }
    }
}
