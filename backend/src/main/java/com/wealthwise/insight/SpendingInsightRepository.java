package com.wealthwise.insight;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface SpendingInsightRepository extends MongoRepository<SpendingInsight, String> {}
