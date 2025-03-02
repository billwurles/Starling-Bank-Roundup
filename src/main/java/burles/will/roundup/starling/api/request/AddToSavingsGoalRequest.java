package burles.will.roundup.starling.api.request;

public record AddToSavingsGoalRequest(burles.will.roundup.starling.api.request.AddToSavingsGoalRequest.Amount amount) {

	public record Amount(String currency, long minorUnits) {}

}
