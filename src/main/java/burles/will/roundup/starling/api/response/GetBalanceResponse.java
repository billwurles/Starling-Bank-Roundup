package burles.will.roundup.starling.api.response;

import lombok.Data;

@Data
public class GetBalanceResponse {

	private Balance clearedBalance;
	private Balance effectiveBalance;
	private Balance pendingTransactions;
	private Balance acceptedOverdraft;
	private Balance amount;
	private Balance totalClearedBalance;
	private Balance totalEffectiveBalance;

	@Data
	public static class Balance {
		private String currency;
		private Long minorUnits;
	}
}
