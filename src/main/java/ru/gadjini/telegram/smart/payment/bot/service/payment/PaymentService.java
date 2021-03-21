package ru.gadjini.telegram.smart.payment.bot.service.payment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.PaidSubscriptionService;
import ru.gadjini.telegram.smart.payment.bot.domain.PaidSubscriptionPlan;

import java.time.LocalDate;

@Service
public class PaymentService {

    private PaidSubscriptionService paidSubscriptionService;

    private PaidSubscriptionPlanService paidSubscriptionPlanService;

    @Autowired
    public PaymentService(PaidSubscriptionService paidSubscriptionService, PaidSubscriptionPlanService paidSubscriptionPlanService) {
        this.paidSubscriptionService = paidSubscriptionService;
        this.paidSubscriptionPlanService = paidSubscriptionPlanService;
    }

    public LocalDate processPayment(int userId) {
        PaidSubscriptionPlan paidSubscriptionPlan = paidSubscriptionPlanService.getPlan();

        return paidSubscriptionService.renewSubscription(userId, paidSubscriptionPlan.getId(), paidSubscriptionPlan.getPeriod());
    }
}
