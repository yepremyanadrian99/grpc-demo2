package am.adrian.grpcdemo2.server.database;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class AccountDatabase {

    private final Map<String, Integer> database = IntStream.range(1, 10)
            .boxed()
            .collect(Collectors.toMap(
                    String::valueOf, value -> value * 100
            ));

    public Integer getBalance(String account) {
        return database.get(account);
    }

    public Integer increaseBalance(String account, Integer valueToIncrease) {
        return changeBalance(account, valueToIncrease, Integer::sum);
    }

    public Integer decreaseBalance(String account, Integer valueToDecrease) {
        return changeBalance(account, -valueToDecrease, Integer::sum);
    }

    private Integer changeBalance(String account,
                                  Integer valueToApply,
                                  BiFunction<Integer, Integer, Integer> applier) {
        return database.computeIfPresent(account, (key, value) -> applier.apply(value, valueToApply));
    }
}
