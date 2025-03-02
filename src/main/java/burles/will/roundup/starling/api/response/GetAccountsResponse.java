package burles.will.roundup.starling.api.response;

import lombok.Data;

import java.util.List;

@Data
public class GetAccountsResponse {
	
	private List<Account> accounts;

	@Data
	public static class Account {
		private String accountUid;
		private String accountType;
		private String defaultCategory;
		private String currency;
		private String createdAt;
		private String name;
	}
}
