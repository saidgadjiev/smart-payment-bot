package ru.gadjini.telegram.smart.payment.bot.service.payment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscriptionPlan;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.PaidSubscriptionPlanService;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.PaidSubscriptionService;

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

    public LocalDate processPayment(int userId, int planId) {
        PaidSubscriptionPlan paidSubscriptionPlan = paidSubscriptionPlanService.getPlanById(planId);

        return paidSubscriptionService.renewSubscription(userId, paidSubscriptionPlan.getId(), paidSubscriptionPlan.getPeriod());
    }
}
