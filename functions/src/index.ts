// functions/src/index.ts (or your main Cloud Functions TypeScript file)

// Firebase Functions SDK for defining Cloud Functions.
import {onCall, HttpsError} from "firebase-functions/v2/https";
import * as logger from "firebase-functions/logger";

// Google AI SDK for Gemini (Node.js)
// Make sure to install this: npm install @google/generative-ai
import {
  GoogleGenerativeAI,
  HarmCategory,
  HarmBlockThreshold,
  GenerationConfig,
  SafetySetting,
  GenerateContentRequest,
  GenerateContentResult, // Corrected: This is what generateContent returns
  GenerateContentResponse, // This is the type of result.response
  Part,
} from "@google/generative-ai";
// For typed environment variables (recommended)
import {defineString} from "firebase-functions/params";


// --- IMPORTANT: Configuration ---
const geminiApiKeyParam = defineString("GEMINI_KEY");
const MODEL_NAME = "gemini-1.0-pro-latest";

// Define interfaces for expected data structures
interface RequestData {
  text?: string;
}

type ListingType =
  | "SELLING_ITEM"
  | "WANTING_TO_BUY_ITEM"
  | "OFFERING_SERVICE"
  | "REQUESTING_SERVICE";

// This interface should match the keys your prompt asks Gemini to return
interface SuggestedPostDetails {
  listingType: ListingType;
  // Renamed to match common LLM output pattern if it generates "suggested..."
  // If your prompt asks for "title", then this should be "title".
  // For consistency with the prompt in the previous version, let's assume these are the keys Gemini returns.
  suggestedTitle: string;
  suggestedDescription: string;
  extractedItemName: string;
  price: number | null;
  currency: string | null;
  suggestedCategory: string | null;
}

interface SuccessResponse {
  success: true;
  data: SuggestedPostDetails; // This will be the 'suggestions' object for the client
}

let genAI: GoogleGenerativeAI | undefined;
const apiKeyFromEnv = process.env.GEMINI_API_KEY;

if (apiKeyFromEnv) {
  try {
    genAI = new GoogleGenerativeAI(apiKeyFromEnv);
    logger.info("GoogleGenerativeAI client initialized globally using GEMINI_API_KEY env var.");
  } catch (e: any) {
    logger.error("Error initializing GoogleGenerativeAI globally:", e.message);
  }
} else {
  logger.warn(
    "GEMINI_API_KEY environment variable not found for global initialization. " +
    "Client will be initialized on first function call using defined param."
  );
}

/**
 * Analyzes user text with Gemini to extract post details.
 * The prompt is dynamically generated based on inputs.
 * @param {RequestData} request - The data sent from the client.
 * @property {string} request.data.text - The user's raw text input.
 * @return {Promise<SuccessResponse>} A promise that resolves with structured post details.
 * @throws {HttpsError} Throws HttpsError on failure.
 */
export const generatePostSuggestions = onCall<
  RequestData,
  Promise<SuccessResponse>
>(async (request) => {
  logger.info("generatePostSuggestions function called. Data received:", request.data);

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

  let dynamicPromptPart = "";
  if (userText.length < 20) {
    dynamicPromptPart += " The user's description is very short, please try to elaborate slightly while staying true to their intent.";
  }

  const prompt = `
Analyze the following user request to create a classifieds-style post.${dynamicPromptPart}
Extract the following information and return it as a VALID JSON object.
Do not include any explanatory text, comments, or markdown formatting like \`\`\`json before or after the JSON object.
The JSON object MUST have these keys:
- "listingType": (string) Choose ONE from: "SELLING_ITEM", "WANTING_TO_BUY_ITEM", "OFFERING_SERVICE", "REQUESTING_SERVICE".
- "suggestedTitle": (string) A concise title for the post (max 10 words).
- "suggestedDescription": (string) A short suggested description for the post (1-2 sentences).
- "extractedItemName": (string) The primary item or service being discussed.
- "price": (number or null) If a price or budget is clearly mentioned in the user's text, extract it as a number. If not mentioned, use null.
- "currency": (string or null) If a currency (e.g., "USD", "EUR", "CHF", "$", "£") is mentioned with a price, extract it. If no currency or price, use null.
- "suggestedCategory": (string or null) A suggested category (e.g., "Electronics", "Vehicles", "Home Services", "Furniture"). If unsure, use null.

User Request: "${userText}"

JSON Output:
    `;

  try {
    logger.info("Sending request to Gemini API with model:", MODEL_NAME);
    logger.info("Prompt being sent:", prompt);

    const model = genAI.getGenerativeModel({model: MODEL_NAME});

    const safetySettings: SafetySetting[] = [
      {category: HarmCategory.HARM_CATEGORY_HARASSMENT, threshold: HarmBlockThreshold.BLOCK_MEDIUM_AND_ABOVE},
      {category: HarmCategory.HARM_CATEGORY_HATE_SPEECH, threshold: HarmBlockThreshold.BLOCK_MEDIUM_AND_ABOVE},
      {category: HarmCategory.HARM_CATEGORY_SEXUALLY_EXPLICIT, threshold: HarmBlockThreshold.BLOCK_MEDIUM_AND_ABOVE},
      {category: HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT, threshold: HarmBlockThreshold.BLOCK_MEDIUM_AND_ABOVE},
    ];

    const generationConfig: GenerationConfig = {
      responseMimeType: "application/json",
    };

    const contentPart: Part = {text: prompt};
    const generateContentRequest: GenerateContentRequest = {
      contents: [{role: "user", parts: [contentPart]}],
      generationConfig,
      safetySettings,
    };

    // Corrected: generateContent returns GenerateContentResult
    const result: GenerateContentResult =
      await model.generateContent(generateContentRequest);

    // Corrected: Access the response property of GenerateContentResult
    const response: GenerateContentResponse = result.response;
    const candidate = response?.candidates?.[0];
    const responseText: string | undefined = candidate?.content?.parts?.[0]?.text;

    if (!responseText) {
      logger.error("Gemini API response candidate is missing content or text part.", JSON.stringify(candidate));
      if (candidate?.finishReason && candidate.finishReason !== "STOP") {
        logger.warn("Gemini generation finished due to: " + candidate.finishReason, JSON.stringify(candidate.safetyRatings));
        throw new HttpsError("internal", `Content generation stopped: ${candidate.finishReason}. Check safety ratings.`);
      }
      throw new HttpsError("internal", "Gemini API response content is invalid.");
    }

    logger.info("Raw response text from Gemini (expected JSON):", responseText);

    let structuredData: SuggestedPostDetails;
    try {
      structuredData = JSON.parse(responseText.trim()) as SuggestedPostDetails;
      logger.info("Successfully parsed JSON from Gemini:", structuredData);
    } catch (error: any) {
      logger.error("Error parsing JSON from Gemini response. Raw text was:", responseText, "Error:", error);
      const jsonMatch = responseText.match(/```json\s*([\s\S]*?)\s*```|({[\s\S]*})/);
      let extractedJsonText: string | null = null;
      if (jsonMatch) {
        extractedJsonText = jsonMatch[1] || jsonMatch[2];
        try {
          if (extractedJsonText) {
            structuredData = JSON.parse(extractedJsonText.trim()) as SuggestedPostDetails;
            logger.info("Successfully parsed JSON after extraction:", structuredData);
          } else {
            throw new Error("JSON match found but extracted text is null.");
          }
        } catch (innerError: any) {
          logger.error("Error parsing extracted JSON:", extractedJsonText, innerError);
          throw new HttpsError(
            "internal",
            "Failed to parse the response from the AI service (after extraction attempt). Raw response: " + responseText
          );
        }
      } else {
        throw new HttpsError(
          "internal",
          "Failed to parse the response from the AI service (no JSON block found). Raw response: " + responseText
        );
      }
    }

    // Corrected: Keys in requiredKeys must match SuggestedPostDetails interface
    const requiredKeys: Array<keyof SuggestedPostDetails> = ["listingType", "suggestedTitle", "suggestedDescription", "extractedItemName"];
    for (const key of requiredKeys) {
      // Corrected: Check against actual keys of SuggestedPostDetails for null allowance
      if (!(key in structuredData) || (structuredData[key] === null && key !== "price" && key !== "currency" && key !== "suggestedCategory")) {
        logger.error(`Missing or null required key '${key}' in parsed JSON.`, structuredData);
        throw new HttpsError("internal", `AI service response missing or invalid key: ${key}`);
      }
    }

    const validListingTypes: ListingType[] = ["SELLING_ITEM", "WANTING_TO_BUY_ITEM", "OFFERING_SERVICE", "REQUESTING_SERVICE"];
    if (!validListingTypes.includes(structuredData.listingType)) {
      logger.error(`Invalid listingType '${structuredData.listingType}' in parsed JSON.`, structuredData);
      throw new HttpsError("internal", `AI service returned invalid listingType: ${structuredData.listingType}`);
    }
    if (structuredData.price !== null && typeof structuredData.price !== "number") {
        logger.warn(`Price from LLM is not a number: ${structuredData.price}. Setting to null.`);
        structuredData.price = null;
    }


    return {
      success: true,
      data: structuredData,
    };
  } catch (error: any) {
    logger.error("Error calling Gemini API or processing response:", error.message, error.stack, error.cause);
    if (error instanceof HttpsError) {
      throw error;
    }
    throw new HttpsError(
      "internal",
      "An unexpected error occurred with the AI service: " + error.message
    );
  }
});
