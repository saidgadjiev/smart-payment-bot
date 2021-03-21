package ru.gadjini.telegram.smart.payment.bot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.payment.bot.dao.PaidSubscriptionPlanDao;
import ru.gadjini.telegram.smart.payment.bot.domain.PaidSubscriptionPlan;

@Service
public class PaidSubscriptionPlanService {

    private PaidSubscriptionPlanDao paidSubscriptionPlanDao;

    @Autowired
    public PaidSubscriptionPlanService(PaidSubscriptionPlanDao paidSubscriptionPlanDao) {
        this.paidSubscriptionPlanDao = paidSubscriptionPlanDao;
    }

    public PaidSubscriptionPlan getPlan() {
        return paidSubscriptionPlanDao.getActivePlan();
    }
}
