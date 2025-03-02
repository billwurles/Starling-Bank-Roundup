package burles.will.roundup.handler.api;

import burles.will.roundup.handler.api.response.RoundUpResponse;
import burles.will.roundup.data.bank.Account;
import burles.will.roundup.starling.StarlingAPIClient;
import burles.will.roundup.starling.api.response.GetSavingsGoalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class ApiController {

	private final StarlingAPIClient client;

	private Account account;

	@GetMapping("/account")
	public Mono<Account> getAccount() {
		return client.getAccountFromStarlingAPI();
	}

	@GetMapping("/account/{accountUid}/savings-goal/{goalUid}")
	public Mono<GetSavingsGoalResponse> getSavingsGoal(@PathVariable String accountUid, @PathVariable String goalUid) {
		return client.getSavingsGoal(accountUid, goalUid);
	}

	@GetMapping("/roundup")
	public Mono<ResponseEntity<RoundUpResponse>> roundUpTransactions(){
		return client.getAccountFromStarlingAPI() // Retrieve the account information: UID, balance & transactions

				.flatMap(account -> {
					long value = account.roundUpPayments(); // Get the value of rounding up the outgoing transactions
					return client.createSavingsGoal( // Create a new savings goal for the roundups
							account.getUid(),
							"Round Ups",
							account.getCurrency()
						)
						.flatMap(goal -> client.addToSavingsGoal( // Transfer the value of the roundups from the main account to the savings goal
								account.getUid(),
								goal.getSavingsGoalUid(),
								account.getCurrency(),
								value
						)
						.flatMap(response -> { // Parse the response
							if (response.getSuccess()) {
								return client.getSavingsGoal( // Get the data from the savings goal for display
										account.getUid(),
										goal.getSavingsGoalUid()
								).flatMap(goalValue -> Mono.just(
										ResponseEntity.ok( // Create an HTTP 200 response with the data needed for a UI to display the result of the roundup
												new RoundUpResponse(
													account,
													new RoundUpResponse.SavingsGoal(
															value,
															goalValue.getSavingsGoalUid(),
															goalValue.getTotalSaved().getMinorUnits()
													)
								))));
							} else {
								return handleRoundUpError( // In case the server sends back an unsuccessful response, throw an error
										501,
										"Error: Round Up Unsuccessful",
										"Attempt to add roundups to savings goal was unsuccessful"
								);
							}
					}));
				})
				.onErrorResume(e -> handleRoundUpError( // If any errors occur in the process, display the relevant error
						500,
						"An error occurred during the roundup process",
						e.getMessage()
				));
	}

	public Mono<ResponseEntity<RoundUpResponse>> handleRoundUpError(int code, String message, String body){ // Method to format errors for display
		return Mono.just(
				ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body(new RoundUpResponse(
								new RoundUpResponse.Error(
										code, message, body)
						))
		);
	}

}
