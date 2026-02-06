package com.example.model.entity.enums;

public enum SubscriptionStatus {
    TRIAL,          // 14-day free trial
    ACTIVE,         // Paid and active
    PAST_DUE,       // Payment failed, grace period
    SUSPENDED,      // Account suspended (no payment)
    CANCELLED       // User cancelled subscription
}