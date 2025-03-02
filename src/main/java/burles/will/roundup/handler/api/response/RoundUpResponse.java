package burles.will.roundup.handler.api.response;

import burles.will.roundup.data.bank.Account;
import lombok.Data;

@Data
public class RoundUpResponse {
	private Boolean success;
	private SavingsGoal savingsGoal;
	private Account account;
	private Error error;

	public RoundUpResponse(Account account, SavingsGoal goal){
		success = true;
		this.account = account;
		this.savingsGoal = goal;
	}

	public RoundUpResponse(Error error){
		success = false;
		this.error = error;
	}

	public record SavingsGoal(long roundUpAmount, String savingsGoalUID, long totalInSavingsGoal) {}

	public record Error(int code, String message, String body) {}
}
