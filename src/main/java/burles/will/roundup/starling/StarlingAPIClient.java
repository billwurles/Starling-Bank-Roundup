package burles.will.roundup.starling;

import burles.will.roundup.data.bank.Account;
import burles.will.roundup.starling.api.request.AddToSavingsGoalRequest;
import burles.will.roundup.starling.api.request.CreateSavingsGoalRequest;
import burles.will.roundup.starling.api.response.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StarlingAPIClient {

	private final WebClient client;

	public static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	private static final String accountsUri = "/accounts/";
	private static final String balanceUri = "/balance";
	private static final String transactionUri = "/statement/downloadForDateRange";
	private static final String accountUri = "/account/";
	private static final String savingsUri = "/savings-goals/";
	private static final String addSavingsUri = "/add-money/";

	public Mono<Account> getAccountFromStarlingAPI(){ // Gets the required account details by running the other API calls
		log.debug("Getting account from Starling API");
		return getAccount().flatMap(// Makes a request to the API for the Account details
				acc -> {
					String uid = acc.getAccounts().get(0).getAccountUid();
					LocalDate now = LocalDate.now();
					LocalDate weekAgo = now.minusDays(7);
					Mono<String> transactionCall = getTransactionsInRange(uid, weekAgo, now); // Gets transactions for the last 7 days
					Mono<GetBalanceResponse> balanceCall = getBalance(uid); // Using the account UID two async API calls are made for balance and transaction data

					return Mono.zip(balanceCall, transactionCall, (balResp, tranResp) ->
							new Account(acc, balResp, tranResp)); // The two async calls are combined into one, which will return an Account class when complete
				});
	}

	public Mono<GetAccountsResponse> getAccount(){ // Gets the account info, importantly UID
		return client.get()
				.uri(accountsUri)
				.retrieve()
				.onStatus(HttpStatusCode::isError, clientResponse ->
						clientResponse.createException().flatMap(e -> Mono.error(
								new WebClientResponseException(
										e.getStatusCode().value(),
										clientResponse.statusCode()+" Error from Starling API getting account",
										e.getHeaders(), e.getResponseBodyAsByteArray(), null)
						)))
				.bodyToMono(GetAccountsResponse.class)
				.onErrorMap(ex -> new Exception("Error occurred parsing response while getting account", ex));
	}

	public Mono<GetBalanceResponse> getBalance(String accountUid){ // Using UID gets the balance data
		return client.get()
				.uri(accountsUri+accountUid+balanceUri)
				.retrieve()
				.onStatus(HttpStatusCode::isError, clientResponse ->
						clientResponse.createException().flatMap(e -> Mono.error(
								new WebClientResponseException(
										e.getStatusCode().value(),
										clientResponse.statusCode()+" Error from Starling API getting balance",
										e.getHeaders(), e.getResponseBodyAsByteArray(), null)
						)))
				.bodyToMono(GetBalanceResponse.class)
				.onErrorMap(ex -> new Exception("Error occurred parsing response while getting balance", ex));
	}

	public Mono<String> getTransactionsInRange(String accountUid, LocalDate startDate, LocalDate endDate){ // Gets the list of transactions within the given range
		String uri = UriComponentsBuilder.fromUriString(accountsUri+accountUid+transactionUri)
				.queryParam("start", startDate.format(dateFormat))
				.queryParam("end", endDate.format(dateFormat))
				.build()
				.toUriString();
		return client.get()
				.uri(uri)
				.header("Accept", "text/csv")
				.retrieve()
				.onStatus(HttpStatusCode::isError, clientResponse ->
						clientResponse.createException().flatMap(e -> Mono.error(
								new WebClientResponseException(
										e.getStatusCode().value(),
										clientResponse.statusCode()+" Error from Starling API getting transactions",
										e.getHeaders(), e.getResponseBodyAsByteArray(), null)
						)))
				.bodyToMono(String.class)
				.onErrorMap(ex -> new Exception("Error occurred parsing response while getting transactions", ex));
	}

	public Mono<CreateSavingsGoalResponse> createSavingsGoal(String accountUid, String name, String currency) { // Creates a new savings goal
		ObjectMapper objectMapper = new ObjectMapper(); // Create the JSON for the savings goal body
		CreateSavingsGoalRequest goal = new CreateSavingsGoalRequest(name, currency);
		String body;
		try {
			body = objectMapper.writeValueAsString(goal);
		} catch (JsonProcessingException e) {
			return Mono.error(handleException(e, "JSON Error occurred building the create savings goal request"));
		}

		return client.put()
				.uri(accountUri+accountUid+savingsUri)
				.bodyValue(body)
				.retrieve()
				.onStatus(HttpStatusCode::isError, clientResponse ->
						clientResponse.createException().flatMap(e -> Mono.error(
								new WebClientResponseException(
										e.getStatusCode().value(),
										clientResponse.statusCode()+" Error from Starling API creating savings goal",
										e.getHeaders(), e.getResponseBodyAsByteArray(), null)
						)))
				.bodyToMono(CreateSavingsGoalResponse.class)
				.onErrorMap(ex -> new Exception("Error occurred parsing response while creating savings goal", ex));
	}

	public Mono<GetSavingsGoalResponse> getSavingsGoal(String accountUid, String savingsUid) { // Gets a savings goal
		return client.get()
				.uri(accountUri+accountUid+savingsUri+savingsUid)
				.retrieve()
				.onStatus(HttpStatusCode::isError, clientResponse ->
						clientResponse.createException().flatMap(e -> Mono.error(
								new WebClientResponseException(
										e.getStatusCode().value(),
										clientResponse.statusCode()+" Error from Starling API getting savings goal",
										e.getHeaders(), e.getResponseBodyAsByteArray(), null)
						)))
				.bodyToMono(GetSavingsGoalResponse.class)
            	.onErrorMap(ex -> new Exception("Error occurred parsing response while getting savings goal", ex));
	}

	public Mono<AddToSavingsGoalResponse> addToSavingsGoal(String accountUid, String savingsUid, String currency, long amount) { // Transfers money into a savings goal
		ObjectMapper objectMapper = new ObjectMapper(); // Create the JSON for the savings amount body
		AddToSavingsGoalRequest.Amount amountNode = new AddToSavingsGoalRequest.Amount(currency, amount);
		AddToSavingsGoalRequest request = new AddToSavingsGoalRequest(amountNode);
		String body;
		try {
			body = objectMapper.writeValueAsString(request);
		} catch (JsonProcessingException e) {
			return Mono.error(handleException(e, "JSON Error occurred building the add to savings goal request"));
		}

		return client.put()
				.uri(accountUri+accountUid+savingsUri+savingsUid+addSavingsUri+UUID.randomUUID())
				.bodyValue(body)
				.retrieve()
				.onStatus(HttpStatusCode::isError, clientResponse ->
						clientResponse.createException().flatMap(e -> Mono.error(
								new WebClientResponseException(
										e.getStatusCode().value(),
										clientResponse.statusCode()+" Error from Starling API adding to savings goal",
										e.getHeaders(), e.getResponseBodyAsByteArray(), null)
						)))
				.bodyToMono(AddToSavingsGoalResponse.class)
            	.onErrorMap(e -> handleException(e, "Error occurred parsing response while adding to savings goal"));
	}

	private Throwable handleException(Throwable e, String message) { // Error handler
		if (e instanceof WebClientResponseException) {
			return new RuntimeException(e.getMessage(), e);
		}
		return new RuntimeException(message, e);
	}
}
