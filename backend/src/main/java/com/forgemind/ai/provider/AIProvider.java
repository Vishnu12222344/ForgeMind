package com.forgemind.ai.provider;

public interface AIProvider {

    AIProviderResponse generate(AIProviderRequest request);
}