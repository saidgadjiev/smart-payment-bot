package ru.gadjini.telegram.smart.payment.bot.service.payment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscription;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscriptionPlan;
import ru.gadjini.telegram.smart.bot.commons.property.SubscriptionProperties;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.PaidSubscriptionPlanService;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.PaidSubscriptionService;
import ru.gadjini.telegram.smart.payment.bot.service.PaidBotApi;

import java.time.LocalDate;
import java.time.ZoneOffset;

@Service
public class PaymentService {

    private PaidSubscriptionService paidSubscriptionService;

    private PaidSubscriptionPlanService paidSubscriptionPlanService;

    private SubscriptionProperties subscriptionProperties;

    private PaidBotApi paidSubscriptionApi;

    @Autowired
    public PaymentService(PaidSubscriptionService paidSubscriptionService,
                          PaidSubscriptionPlanService paidSubscriptionPlanService,
                          SubscriptionProperties subscriptionProperties, PaidBotApi paidSubscriptionApi) {
        this.paidSubscriptionService = paidSubscriptionService;
        this.paidSubscriptionPlanService = paidSubscriptionPlanService;
        this.subscriptionProperties = subscriptionProperties;
        this.paidSubscriptionApi = paidSubscriptionApi;
    }

    public PaidSubscription processPayment(long userId, int planId) {
        PaidSubscriptionPlan paidSubscriptionPlan = paidSubscriptionPlanService.getPlanById(planId);

        PaidSubscription paidSubscription = paidSubscriptionService.renewSubscription(subscriptionProperties.getPaidBotName(),
                userId, paidSubscriptionPlan.getId(), paidSubscriptionPlan.getPeriod());

        paidSubscriptionApi.refreshSub(userId);

        return paidSubscription;
    }
}
