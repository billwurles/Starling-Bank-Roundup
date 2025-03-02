package burles.will.roundup.starling;

import burles.will.roundup.data.bank.Account;
import burles.will.roundup.starling.api.response.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations="classpath:application.properties")
class StarlingAPIClientTest {

	StarlingAPIClient client;
	final String uid = "b822bcc5-f555-4e99-b5a3-635b36cb1d6a";

	@Value("${starling.base.url}")
	private String starlingBaseUrl;
	@Value("${starling.auth.token}")
	private String starlingAuthToken;

	@BeforeEach
	void setUp() {
		client = new StarlingAPIClient(WebClient.builder().baseUrl(starlingBaseUrl) // Base API URL
				.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + starlingAuthToken)
				.build());
	}

	@Test
	void getAccountFromStarlingAPI() {
		Mono<Account> accountResp = client.getAccountFromStarlingAPI();
		Account acc = accountResp.block();
		assert acc != null;
		System.out.println(acc.toString());

		assertEquals(uid, acc.getUid());
		assertEquals("GBP", acc.getCurrency());
		assertEquals(50000L, acc.getTransactions().get(0).value());
		assertEquals("Ref: 8136181412", acc.getTransactions().get(0).ref());
		assertEquals(-1097L, acc.getTransactions().get(8).value());
		assertEquals("2025-03-01", acc.getTransactions().get(8).date().format(StarlingAPIClient.dateFormat));
	}

	@Test
	void getAccount() {
		GetAccountsResponse dto = client.getAccount().block();
		System.out.println(dto.toString());
		GetAccountsResponse.Account account = dto.getAccounts().get(0);
		assertEquals(uid, account.getAccountUid());
		assertEquals("GBP", account.getCurrency());
	}

	@Test
	void getBalance() {
		GetBalanceResponse dto = client.getBalance(uid).block();
		System.out.println(dto.toString());
		GetBalanceResponse.Balance balance = dto.getEffectiveBalance();
		assertInstanceOf(Long.class, balance.getMinorUnits());
		assertEquals("GBP", balance.getCurrency());
	}

	@Test
	void getTransactionsInRange() {
		LocalDate time = LocalDate.of(2025, 3, 1);
		String csv = client.getTransactionsInRange(uid, time, time).block();
		System.out.println(csv);
		String[] rows = csv.split("\n");
		String[] transaction1 = rows[1].split(",");
		String[] transaction2 = rows[9].split(",");

		assertEquals(50000L, Long.parseLong(transaction1[4].replace(".","")));
		assertEquals(-1097L, Long.parseLong(transaction2[4].replace(".","")));
		assertEquals(3L, (Long.parseLong(transaction2[4].replace(".","")) % 100) +100 );
	}


	@Test
	void createSavingsGoal() {
		CreateSavingsGoalResponse response = client.createSavingsGoal(uid, "Test goal", "GBP").block();
		System.out.println(response.toString());

		assert(response.getSuccess());
	}

	@Test
	void getSavingsGoal() {
		String currency = "GBP";
		String savingsUid = client.createSavingsGoal(uid, "Test goal", currency)
				.block()
				.getSavingsGoalUid();

		GetSavingsGoalResponse goal = client.getSavingsGoal(uid, savingsUid).block();
		System.out.println(goal.toString());

		assertEquals(savingsUid, goal.getSavingsGoalUid());
		assertEquals(currency, goal.getTotalSaved().getCurrency());
		assertEquals("ACTIVE", goal.getState());
	}

	@Test
	void addToSavingsGoal() {
		long savings = 100L;

		String savingsUid = client.createSavingsGoal(uid, "Test goal", "GBP")
				.block()
				.getSavingsGoalUid();

		AddToSavingsGoalResponse response = client.addToSavingsGoal(uid, savingsUid, "GBP", savings).block();
		System.out.println(response.toString());

		assert(response.getSuccess());

		GetSavingsGoalResponse goal = client.getSavingsGoal(uid, savingsUid).block();
		System.out.println(goal.toString());

		assertEquals(savings, goal.getTotalSaved().getMinorUnits());
	}
}