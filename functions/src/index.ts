import {HttpsError, onCall} from "firebase-functions/v2/https";
import * as logger from "firebase-functions/logger";

import {
    GenerateContentRequest,
    GenerateContentResult,
    GenerationConfig,
    GoogleGenerativeAI,
    HarmBlockThreshold,
    HarmCategory,
    Part,
    SafetySetting,
} from "@google/generative-ai";
import {defineString} from "firebase-functions/params";

const geminiApiKeyParam = defineString("GEMINI_KEY");

const MODEL_NAME = "gemini-pro";

interface RequestData {
    text?: string;
}

type ListingType =
    | "SELLING_ITEM"
    | "WANTING_TO_BUY_ITEM"
    | "OFFERING_SERVICE"
    | "REQUESTING_SERVICE";

interface SuggestedPostDetails {
    listingType: ListingType;
    title: string;
    description: string;
    itemName: string;
    price: number | null;
    currency: string | null;
    category: string | null;
}

interface SuccessResponse {
    success: true;
    data: SuggestedPostDetails;
}

let genAI: GoogleGenerativeAI | undefined;
const apiKeyFromEnv = process.env.GEMINI_API_KEY; // Standard Node.js way

if (apiKeyFromEnv) {
    try {
        genAI = new GoogleGenerativeAI(apiKeyFromEnv);
        logger.info("GoogleGenerativeAI client initialized globally using GEMINI_API_KEY env var.");
    } catch (e: any) {
        logger.error("Error initializing GoogleGenerativeAI globally:", e.message);
    }
} else {
    logger.warn(
        "GEMINI_API_KEY environment variable not found for global initialization." +
        "Client will be initialized on first function call using defined param."
    );
}


/**
 * Analyzes user text with Gemini to extract post details.
 * @param {RequestData} request - The data sent from the client.
 * @property {string} request.data.text - The user's raw text input.
 * @return {Promise<SuccessResponse>} A promise that resolves with structured post details.
 * @throws {HttpsError} Throws HttpsError on failure.
 */
export const analyzePostText = onCall<
    RequestData,
    Promise<SuccessResponse>
>(async (request) => {
    logger.info("analyzePostText function called. Data received:", request.data);

    // Initialize genAI client if not already done, using the defined param
    if (!genAI) {
        const currentApiKey = geminiApiKeyParam.value();
        if (currentApiKey) {
            logger.info("Initializing genAI client within onCall with GEMINI_KEY param.");
            try {
                genAI = new GoogleGenerativeAI(currentApiKey);
            } catch (e: any) {
                logger.error("Error initializing GoogleGenerativeAI in onCall:", e.message);
                throw new HttpsError("internal", "Gemini AI service could not be initialized due to API key issue.");
            }
        } else if (apiKeyFromEnv) { // Fallback if param somehow not resolved but env var was
            logger.warn("GEMINI_KEY param was not resolved, but GEMINI_API_KEY env var was found. Using env var for onCall initialization.");
            genAI = new GoogleGenerativeAI(apiKeyFromEnv);
        } else {
            logger.error(
                "Gemini API Key is not configured via param GEMINI_KEY or env var GEMINI_API_KEY."
            );
            throw new HttpsError(
                "internal",
                "Gemini AI service is not configured."
            );
        }
    }


    const userText = request.data.text;
    if (!userText || typeof userText !== "string" || userText.trim() === "") {
        logger.error("Invalid input: 'text' field is missing or empty.");
        throw new HttpsError(
            "invalid-argument",
            "The function must be called with a 'text' argument containing the user's input."
        );
    }

    const prompt = `
Analyze the following user request to create a classifieds-style post.
Extract the following information and return it as a VALID JSON object.
Do not include any explanatory text before or after the JSON object.
The JSON object should have these keys:
- "listingType": (string) Choose ONE from: "SELLING_ITEM", "WANTING_TO_BUY_ITEM", "OFFERING_SERVICE", "REQUESTING_SERVICE".
- "title": (string) A concise title for the post (max 10 words).
- "description": (string) A short suggested description for the post (1-2 sentences).
- "itemName": (string) The primary item or service being discussed.
- "price": (number or null) Any mentioned price or budget as a number. If no price, use null.
- "currency": (string or null) Any mentioned currency (e.g., "USD", "EUR", "CHF", "$", "£"). If no currency, use null.
- "category": (string or null) A suggested category (e.g., "Electronics", "Vehicles", "Home Services", "Furniture"). If unsure, use null.

User Request: "${userText}"

JSON Output:
    `;

    try {
        logger.info("Sending request to Gemini API...");
        const model = genAI.getGenerativeModel({model: MODEL_NAME});

        const safetySettings: SafetySetting[] = [
            {category: HarmCategory.HARM_CATEGORY_HARASSMENT, threshold: HarmBlockThreshold.BLOCK_MEDIUM_AND_ABOVE},
            {category: HarmCategory.HARM_CATEGORY_HATE_SPEECH, threshold: HarmBlockThreshold.BLOCK_MEDIUM_AND_ABOVE},
            {
                category: HarmCategory.HARM_CATEGORY_SEXUALLY_EXPLICIT,
                threshold: HarmBlockThreshold.BLOCK_MEDIUM_AND_ABOVE
            },
            {
                category: HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT,
                threshold: HarmBlockThreshold.BLOCK_MEDIUM_AND_ABOVE
            },
        ];

        const generationConfig: GenerationConfig = {
            // temperature: 0.9,
            // maxOutputTokens: 2048,
            // responseMimeType: "application/json",
        };

        const contentPart: Part = {text: prompt};
        const generateContentRequest: GenerateContentRequest = {
            contents: [{role: "user", parts: [contentPart]}],
            generationConfig,
            safetySettings,
        };

        const result: GenerateContentResult =
            await model.generateContent(generateContentRequest);

        // Use ContentCandidate as the type for an individual candidate
        const candidate = result.response?.candidates?.[0];
        const responseText: string | undefined = candidate?.content?.parts?.[0]?.text;

        if (!responseText) {
            logger.error("Gemini API response candidate is missing content or text part.", candidate);
            if (candidate?.finishReason && candidate.finishReason !== "STOP") {
                logger.warn("Gemini generation finished due to: " + candidate.finishReason, candidate.safetyRatings);
                throw new HttpsError("internal", `Content generation stopped: ${candidate.finishReason}. Check safety ratings.`);
            }
            throw new HttpsError("internal", "Gemini API response content is invalid.");
        }

        logger.info("Raw response text from Gemini:", responseText);

        const jsonMatch = responseText.match(/```json\s*([\s\S]*?)\s*```|({[\s\S]*})/);
        let extractedJsonText: string | null = null;
        if (jsonMatch) {
            extractedJsonText = jsonMatch[1] || jsonMatch[2];
        } else {
            extractedJsonText = responseText;
        }

        if (!extractedJsonText) {
            logger.error("Could not extract JSON from LLM response:", responseText);
            throw new HttpsError("internal", "LLM response was not in the expected JSON format.");
        }

        let structuredData: SuggestedPostDetails;
        try {
            structuredData = JSON.parse(extractedJsonText.trim()) as SuggestedPostDetails;
            logger.info("Successfully parsed JSON from Gemini:", structuredData);
        } catch (error: any) {
            logger.error("Error parsing JSON from Gemini response:", extractedJsonText, error);
            throw new HttpsError(
                "internal",
                "Failed to parse the response from the AI service. Raw response: " + extractedJsonText
            );
        }

        const requiredKeys: Array<keyof SuggestedPostDetails> = ["listingType", "title", "description", "itemName"];
        for (const key of requiredKeys) {
            if (!(key in structuredData) || structuredData[key] === undefined || structuredData[key] === null && key !== "price" && key !== "currency" && key !== "category") {
                logger.error(`Missing or invalid required key '${key}' in parsed JSON.`, structuredData);
                throw new HttpsError("internal", `AI service response missing or invalid key: ${key}`);
            }
        }

        const validListingTypes: ListingType[] = ["SELLING_ITEM", "WANTING_TO_BUY_ITEM", "OFFERING_SERVICE", "REQUESTING_SERVICE"];
        if (!validListingTypes.includes(structuredData.listingType)) {
            logger.error(`Invalid listingType '${structuredData.listingType}' in parsed JSON.`, structuredData);
            throw new HttpsError("internal", `AI service returned invalid listingType: ${structuredData.listingType}`);
        }

        return {
            success: true,
            data: structuredData,
        };
    } catch (error: any) {
        logger.error("Error calling Gemini API or processing response:", error.message, error.stack);
        if (error instanceof HttpsError) {
            throw error;
        }
        throw new HttpsError(
            "internal",
            "An unexpected error occurred: " + error.message,
        );
    }
});
