
# Precautions for Generating AI Responses in User Availability Prediction Project

## Context

In our capstone project, which focuses on predicting user availability for responding to calls or messages by utilizing mobile sensor data, it is crucial that any prompts given to ChatGPT or similar AI tools remain aligned with the provided dataset. The dataset shared by our professor consists of structured observations, including screen status, ringer mode, time of day, and brief behavioral summaries like “Bob has not been checking his notifications.”

## Guidelines

When formulating prompts for ChatGPT to assess user availability, we must refrain from introducing any assumptions or speculative scenarios that are not reflected in the dataset. For instance, prompts such as “User is resting” or “User is working on a computer” are not present in the data and thus can be categorized as fabricated or additionally generated inputs.

These situations can lead to availability predictions from ChatGPT that, while they might seem reasonable, are not based on actual data and can skew system performance.

## Validation Framework

To reduce this risk, we have established a validation framework that analyzes each prompt for its adherence to the terminology and structure of the dataset. Prompts containing unsupported terms are either rejected or flagged for rephrasing.

## Conclusion

By sticking to validated phrasing derived from the dataset and steering clear of imaginative or unverified assumptions, we ensure that AI tools such as ChatGPT remain reliable and evaluate user availability grounded solely in real data.
