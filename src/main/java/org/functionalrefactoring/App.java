package org.functionalrefactoring;

import io.vavr.control.Option;
import org.functionalrefactoring.models.*;

import java.math.BigDecimal;
import java.util.function.Function;

public class App {
    public static void applyDiscount(CartId cartId, Storage<Cart> storage) {
        lift(App::loadCart,cartId)
        .map(cart -> lift(App::lookupDiscountRule,cart.customerId)
        .map(rule -> rule.apply(cart) )
        .map(discount -> updateAmount(cart, discount))
        .peek(updatedCart -> save(updatedCart, storage)));
     }

    private static Cart loadCart(CartId id) {
        if (id.value.contains("gold"))
            return new Cart(id, new CustomerId("gold-customer"), new Amount(new BigDecimal(100)));
        if (id.value.contains("normal"))
            return new Cart(id, new CustomerId("normal-customer"), new Amount(new BigDecimal(100)));
        return null;
    }

    private static <A,B> Option<B> lift(Function<A, B> f, A a)
    {
      return lift(f).apply(a);
    }

    private static <A,B> Function<A, Option<B>> lift(Function<A, B> func)
    {
        return customerId -> {
            B apply = func.apply(customerId);
            return Option.when(apply != null,apply);
        };
    }

    private static DiscountRule lookupDiscountRule(CustomerId id) {

        return (id.value.contains("gold")) ? new DiscountRule(App::half) : null;

    }

    private static Cart updateAmount(Cart cart, Amount discount) {
        return new Cart(cart.id, cart.customerId, new Amount(cart.amount.value.subtract(discount.value)));
    }

    private static void save(Cart cart, Storage<Cart> storage) {
        storage.flush(cart);
    }

    private static Amount half(Cart cart) {
        return new Amount(cart.amount.value.divide(new BigDecimal(2)));
    }
}
