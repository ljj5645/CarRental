package carrental.external;

import org.springframework.stereotype.Component;

@Component
public class PayServiceFallback implements PayService {
    @Override
    public boolean pay(Pay pay) {
        //do nothing if you want to forgive it

        System.out.println("Circuit breaker has been opened. Fallback returned instead.");
        return false;
    }
}