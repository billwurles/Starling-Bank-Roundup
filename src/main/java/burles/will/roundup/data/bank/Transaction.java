package burles.will.roundup.data.bank;

import java.time.LocalDate;

public record Transaction(long value, LocalDate date, String ref) {

}
