package com.dan.junit.extension;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

public class ConditionalExtension implements ExecutionCondition {

    /**
     * Метод, позволяющий описывать условие - стоит ли вызывать тест
     * @param extensionContext
     * @return
     */
    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext extensionContext) {
        return System.getProperty("skip") != null // можно делать более сложные условия, используя ExtensionContext
                ? ConditionEvaluationResult.disabled("test is skipped")
                : ConditionEvaluationResult.enabled("enable by default");
    }
}
