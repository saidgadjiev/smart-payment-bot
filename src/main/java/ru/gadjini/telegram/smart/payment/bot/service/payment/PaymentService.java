package ru.gadjini.telegram.smart.payment.bot.service.payment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscription;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscriptionPlan;
import ru.gadjini.telegram.smart.bot.commons.property.SubscriptionProperties;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.PaidSubscriptionPlanService;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.PaidSubscriptionService;

import java.time.LocalDate;
import java.time.ZoneOffset;

@Service
public class PaymentService {

    private PaidSubscriptionService paidSubscriptionService;

    private PaidSubscriptionPlanService paidSubscriptionPlanService;

    private SubscriptionProperties subscriptionProperties;

    @Autowired
    public PaymentService(PaidSubscriptionService paidSubscriptionService,
                          PaidSubscriptionPlanService paidSubscriptionPlanService,
                          SubscriptionProperties subscriptionProperties) {
        this.paidSubscriptionService = paidSubscriptionService;
        this.paidSubscriptionPlanService = paidSubscriptionPlanService;
        this.subscriptionProperties = subscriptionProperties;
    }

    public PaidSubscription processPayment(int userId, int planId) {
        PaidSubscriptionPlan paidSubscriptionPlan = paidSubscriptionPlanService.getPlanById(planId);

        return paidSubscriptionService.renewSubscription(subscriptionProperties.getPaidBotName(),
                userId, paidSubscriptionPlan.getId(), paidSubscriptionPlan.getPeriod());
    }

    public CheckoutValidationResult validateCheckout(int userId) {
        PaidSubscription subscription = paidSubscriptionService.getSubscription(subscriptionProperties.getPaidBotName(), userId);

        if (subscription == null || subscription.isTrial()) {
            return CheckoutValidationResult.OK;
        } else if (subscription.isActive()) {
            LocalDate endDate = subscription.getEndDate();
            LocalDate checkoutDate = endDate.minusDays(5);
            LocalDate now = LocalDate.now(ZoneOffset.UTC);

            if (now.isBefore(checkoutDate)) {
                return new CheckoutValidationResult(endDate, checkoutDate.plusDays(1));
            }
        }

        return CheckoutValidationResult.OK;
    }

    public static class CheckoutValidationResult {

        private static final CheckoutValidationResult OK = new CheckoutValidationResult();

        private LocalDate subscriptionEndDate;

        private LocalDate nextCheckoutDate;

        private boolean valid;

        private CheckoutValidationResult() {
            this.valid = true;
        }

        private CheckoutValidationResult(LocalDate subscriptionEndDate, LocalDate nextCheckoutDate) {
            this.subscriptionEndDate = subscriptionEndDate;
            this.nextCheckoutDate = nextCheckoutDate;
            this.valid = false;
        }

        public LocalDate getSubscriptionEndDate() {
            return subscriptionEndDate;
        }

        public LocalDate getNextCheckoutDate() {
            return nextCheckoutDate;
        }

        public boolean isValid() {
            return valid;
        }

        public boolean isInValid() {
            return !valid;
        }
    }
}
