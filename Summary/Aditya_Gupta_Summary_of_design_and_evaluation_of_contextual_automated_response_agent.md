**Summary - Laila is in a Meeting: Design and Evaluation of a Contextual Auto-Response Messaging Agent**

This research paper presents the design, implementation, and evaluation of an automated response agent aimed at managing user availability in mobile messaging applications. The agent leverages contextual information (e.g., phone usage, location, calendar events, and ambient noise) to predict user unavailability and send auto-responses to incoming messages. The goal is to reduce distractions, improve situational awareness for message senders, and maintain user privacy while fostering trust in the system.

Key Contributions:
    1. Agent Design:
        o The agent is fully automated, using personalized machine learning models (XGBoost) to predict user availability based on smartphone sensor data and usage patterns.
        o It sends context-aware auto-responses within the same messaging thread, informing senders of the recipient’s unavailability and providing relevant context (e.g., "Laila may not be available; she is in a meeting").
        o The agent supports multiple messaging applications (e.g., WhatsApp, Slack, Facebook Messenger) and operates without parsing message content to ensure privacy.
    2. Privacy Considerations:
        o The agent shares mid-level contextual information (e.g., "noisy environment" or "silent mode") rather than detailed sensor data or high-level inferences (e.g., "sleeping").
        o It ensures mutual awareness by sending auto-responses only when a new messaging session is initiated and the user is predicted to be unavailable, limiting unnecessary sharing of status information.
    3. User Study:
        o A two-week user study with 12 participants evaluated the agent’s effectiveness. Participants reported:
            ▪ Reduced pressure to respond immediately to messages.
            ▪ Improved focus on tasks, especially in contexts like driving, studying, or attending appointments.
            ▪ Fewer explanations needed for delayed responses, as the agent provided context for unavailability.
        o However, the agent’s utility varied based on:
            ▪ Message urgency: Some participants preferred handling urgent messages themselves.
            ▪ Contact relationships: Auto-responses were more useful for distant contacts or higher-authority figures (e.g., bosses) than close friends or family.
            ▪ Contextual relevance: Participants found certain auto-responses (e.g., phone usage, ringer mode) more convincing and less privacyinvasive than others (e.g., app usage, noise levels).
    4. Challenges and Misinterpretations:
        o Some auto-responses led to misinterpretations (e.g., "noisy environment" was misinterpreted as being at a party).
        o Participants expressed concerns about privacy implications of sharing app usage or location data, even at a categorical level (e.g., "entertainment app").
        o False positives (auto-responses sent when the user was available) and false negatives (no auto-response when the user was unavailable) reduced trust in the agent.
    5. Design Implications:
        o Cooperative Human-Agent Interaction: The agent should allow users to provide feedback and customize auto-responses to improve accuracy and relevance.
        o Transparency and Explainability: Making the agent’s decision-making process transparent can help users understand and trust its actions, fostering better appropriation of the technology.
        o Long-Term Adaptation: Future work should explore long-term usage to understand habituation, over-reliance, and evolving user-contract dynamics.
    6. Limitations and Future Work:
        o The study was conducted during the COVID-19 pandemic, which may have influenced results due to increased phone usage and remote work.
        o The agent operated alongside existing availability indicators in messaging apps, which could have affected its perceived utility.
        o Future research could explore message content parsing (with privacy safeguards) to improve context awareness and prevent auto-responses in sensitive situations (e.g. serious conversations).

Conclusion:
The automated response agent demonstrates promising potential to reduce distractions and improve communication efficiency by managing user availability in mobile messaging. However, its success depends on contextual relevance, privacy considerations, and user trust. By incorporating user feedback, improving transparency, and adapting to long-term
usage patterns, intelligent personal assistants like this agent can become more effective tools for managing digital communication.
