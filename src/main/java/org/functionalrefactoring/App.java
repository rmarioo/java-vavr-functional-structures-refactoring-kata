package org.functionalrefactoring;

import io.vavr.control.Option;
import org.functionalrefactoring.models.*;

import java.math.BigDecimal;
import java.util.function.Function;

public class App {
    public static void applyDiscount(CartId cartId, Storage<Cart> storage) {
        loadCart(cartId)
        .map(cart -> lookupDiscountRule(cart)
        .map(rule -> rule.apply(cart) )
        .map(discount -> updateAmount(cart, discount))
        .peek(updatedCart -> save(updatedCart, storage)));
     }

    private static Option<DiscountRule> lookupDiscountRule(Cart cart) {
        return liftNull(App::partialLookupDiscountRule,cart.customerId);
    }

    private static Option<Cart> loadCart(CartId cartId) {
        return liftNull(App::partialLoadCart,cartId);
    }

    private static Cart partialLoadCart(CartId id) {
        if (id.value.contains("gold"))
            return new Cart(id, new CustomerId("gold-customer"), new Amount(new BigDecimal(100)));
        if (id.value.contains("normal"))
            return new Cart(id, new CustomerId("normal-customer"), new Amount(new BigDecimal(100)));
        return null;
    }

    private static <A,B> Option<B> liftNull(Function<A, B> f, A a)
    {
      return liftNull(f).apply(a);
    }

    private static <A,B> Function<A, Option<B>> liftNull(Function<A, B> func)
    {
        return value -> {
            B apply = func.apply(value);
            return Option.when(apply != null,apply);
        };
    }

    private static DiscountRule partialLookupDiscountRule(CustomerId id) {

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
