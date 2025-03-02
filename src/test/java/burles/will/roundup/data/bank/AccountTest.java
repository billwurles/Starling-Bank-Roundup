package burles.will.roundup.data.bank;

import burles.will.roundup.starling.StarlingAPIClient;
import burles.will.roundup.starling.api.response.GetAccountsResponse;
import burles.will.roundup.starling.api.response.GetBalanceResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

	Account account;

	@BeforeEach
	public void setUp() throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		GetAccountsResponse acc = objectMapper.readValue(accountResp, GetAccountsResponse.class);
		GetBalanceResponse bal = objectMapper.readValue(balanceResp, GetBalanceResponse.class);
		account = new Account(acc,bal,transactionResp);
	}

	@Test
	public void constructorTest() {
		assertEquals("USD", account.getCurrency());
		assertEquals(12345L, account.getTransactions().get(0).value());
		assertEquals("Ref: hello", account.getTransactions().get(1).ref());
		assertEquals(-98765L, account.getTransactions().get(1).value());
		assertEquals("2011-11-11", account.getTransactions().get(2).date().format(StarlingAPIClient.dateFormat));
	}

	@Test
	void roundUpPayments() {
		long total = account.roundUpPayments();
		assertEquals(225L, total);
	}


	String accountResp = """
{
  "accounts" : [ {
    "accountUid" : "b822bcc5-f555-4e99-b5a3-635b36cb1d6a",
    "accountType" : "PRIMARY",
    "defaultCategory" : "b8224e85-a617-4909-a934-1ebe027125e6",
    "currency" : "USD",
    "createdAt" : "2025-03-01T15:30:24.445Z",
    "name" : "Personal"
  } ]
}
""";

	String transactionResp = """
Date,Counter Party,Reference,Type,Amount (GBP),Balance (GBP),Spending Category,Notes
01/03/2025,Faster payment,Ref: 8136181412,FASTER PAYMENT,123.45,500.00,INCOME,
01/03/2025,Faster payment,Ref: hello,FASTER PAYMENT,-987.65,2500.00,INCOME,
11/11/2011,Faster payment,Ref: 5302300096,FASTER PAYMENT,17.61,2517.61,INCOME,
01/03/2025,Mickey Mouse,External Payment,FASTER PAYMENT,-10.97,2620.41,PAYMENTS,
01/03/2025,Mickey Mouse,External Payment,FASTER PAYMENT,-16.51,2603.90,PAYMENTS,
01/03/2025,Mickey Mouse,External Payment,FASTER PAYMENT,-21.53,2582.37,PAYMENTS,
01/03/2025,Mickey Mouse,External Payment,FASTER PAYMENT,-21.45,2560.92,PAYMENTS,
01/03/2025,Mickey Mouse,External Payment,FASTER PAYMENT,-1.72,2559.20,PAYMENTS,
01/03/2025,Mickey Mouse,External Payment,FASTER PAYMENT,-14.92,2544.28,PAYMENTS,
""";

	String balanceResp = """
{
  "clearedBalance" : {
    "currency" : "GBP",
    "minorUnits" : 11
  },
  "effectiveBalance" : {
    "currency" : "GBP",
    "minorUnits" : 1000
  },
  "pendingTransactions" : {
    "currency" : "GBP",
    "minorUnits" : -112
  },
  "acceptedOverdraft" : {
    "currency" : "GBP",
    "minorUnits" : 0
  },
  "amount" : {
    "currency" : "GBP",
    "minorUnits" : -1213123
  },
  "totalClearedBalance" : {
    "currency" : "GBP",
    "minorUnits" : 234809234
  },
  "totalEffectiveBalance" : {
    "currency" : "GBP",
    "minorUnits" : 51235234
  }
}
""";
}