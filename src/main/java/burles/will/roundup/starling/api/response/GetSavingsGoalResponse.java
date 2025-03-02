package burles.will.roundup.starling.api.response;

import lombok.Data;

@Data
public class GetSavingsGoalResponse {
	private String savingsGoalUid;
	private String success;
	private Total totalSaved;
	private String state;

	@Data
	public static class Total {
		private String currency;
		private Long minorUnits;
	}
}
