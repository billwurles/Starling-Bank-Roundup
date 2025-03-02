package burles.will.roundup.data.bank;

import burles.will.roundup.starling.api.response.GetAccountsResponse;
import burles.will.roundup.starling.api.response.GetBalanceResponse;
import lombok.Getter;

import java.time.LocalDate;
import java.util.ArrayList;

@Getter
public class Account {

	private final String uid;
	private final String name;
	private final String currency;
	private final long balance;
	private final ArrayList<Transaction> transactions;

	public Account(GetAccountsResponse accounts, GetBalanceResponse balances, String transactionsCSV){ // Builds the account object from the various API responses
		uid = accounts.getAccounts().get(0).getAccountUid();
		name = accounts.getAccounts().get(0).getName();
		currency = accounts.getAccounts().get(0).getCurrency();

		GetBalanceResponse.Balance effective = balances.getEffectiveBalance();
		balance = effective.getMinorUnits();

		transactions = new ArrayList<>();
		String[] rows = transactionsCSV.split("\n");
		for(String row : rows){
			String[] columns = row.split(",");
			if(!columns[0].equals("Date")) {
				String[] date = columns[0].split("/");
				transactions.add(new Transaction(
						Long.parseLong(columns[4].replace(".", "")), //Get transaction value
						LocalDate.of(Integer.parseInt(date[2]), Integer.parseInt(date[1]), Integer.parseInt(date[0])), // Transaction date
						columns[2])); // Transaction reference
			}
		}
	}

	public long roundUpPayments(){
		return transactions.stream()
				.mapToLong(Transaction::value)
				.filter(value -> value < 0) 			// Only roundup money out
				.map(value -> (value % 100) + 100) 	// Get the remaining pennies, and make it positive
				.sum();
	}
}

