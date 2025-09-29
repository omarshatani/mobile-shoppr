// functions/src/index.ts

import {onCall, HttpsError} from "firebase-functions/v2/https";
import * as logger from "firebase-functions/logger";
import {
  GoogleGenerativeAI,
  HarmCategory,
  HarmBlockThreshold,
  GenerationConfig,
  SafetySetting,
  GenerateContentRequest,
  GenerateContentResult,
  Part,
} from "@google/generative-ai";
import {defineString} from "firebase-functions/params";

const geminiApiKeyParam = defineString("SECRETS_GEMINI_API_KEY");
const META_PROMPT_MODEL_NAME = "gemini-2.5-pro";
const SUGGESTION_MODEL_NAME = "gemini-2.5-pro";

interface ClientRequestData {
  text?: string;
  imageUrls?: string[];
  baseOfferPrice?: string;
  baseOfferCurrency?: string;
}

type ListingType =
  | "SELLING_ITEM"
  | "WANTING_TO_BUY_ITEM"
  | "WANTING_TO_OFFER_SERVICE"
  | "OFFER_TO_BUY_SERVICE"
  | "UNKNOWN";

interface LLMSuggestions {
  listingType: ListingType;
  suggestedTitle: string;
  suggestedDescription: string;
  extractedItemName: string;
  price: number | null;
  currency: string | null;
  suggestedCategories: string[] | null; // Changed from suggestedCategory: string
}

interface CloudFunctionSuccessResponse {
  success: true;
  data: LLMSuggestions;
}

let genAI: GoogleGenerativeAI | undefined;

const initializeGenAI = (): GoogleGenerativeAI => {
  if (genAI) {
    return genAI;
  }
  const apiKey = geminiApiKeyParam.value() || process.env.SECRETS_GEMINI_API_KEY;
  if (apiKey) {
    logger.info("Initializing GoogleGenerativeAI client.");
    genAI = new GoogleGenerativeAI(apiKey);
    return genAI;
  } else {
    logger.error("Gemini API Key not found in Firebase config or env for SECRETS_GEMINI_API_KEY.");
    throw new HttpsError("internal", "AI service API key not configured.");
  }
};

/**
 * Generates a dynamic prompt for the main LLM call.
 * @param {ClientRequestData} clientData The data from the client.
 * @return {Promise<string>} The dynamically generated prompt.
 */
async function generateDynamicPromptForSuggestions(clientData: ClientRequestData): Promise<string> {
  const localGenAI = initializeGenAI();
  const model = localGenAI.getGenerativeModel({model: META_PROMPT_MODEL_NAME});

  let metaContext = `User text: "${clientData.text}"\n`;
  if (clientData.imageUrls && clientData.imageUrls.length > 0) {
    metaContext += `User has provided ${clientData.imageUrls.length} image(s).\n`;
  }
  if (clientData.baseOfferPrice) {
    metaContext += `User has indicated a base offer/price of ${clientData.baseOfferPrice} ${clientData.baseOfferCurrency || ""}.\n`;
  }

  const metaPrompt = `
You are an expert prompt engineer. A user wants to create a classifieds post.
User's initial input context:
${metaContext}

Based on this input, create an optimized and detailed prompt for another AI assistant. This optimized prompt must instruct the second AI to:
1. Analyze the original user request text.
2. Determine the "listingType". This can be one of the following: "SELLING_ITEM", "WANTING_TO_BUY_ITEM", "WANTING_TO_OFFER_SERVICE", "OFFER_TO_BUY_SERVICE" or "UNKNOWN".
3. Generate a "suggestedTitle" (5-10 words, concise and appealing). The title should not mention the price.
4. Generate a "suggestedDescription" (1-3 informative sentences).
5. Extract the "extractedItemName" (the primary item or service).
6. Extract a "price" (as a number, or null if not explicitly stated in the user's text).
7. Extract a "currency" (e.g., "USD", "EUR", "CHF", or null if no price/currency is stated in the user's text).
8. Generate "suggestedCategories", which must be an array of up to 3 relevant categories chosen ONLY from the following list: [Electronics, Vehicles, Property, Home & Garden, Fashion, Hobbies & Leisure, Services, Jobs, Pets, Travel, Other].
9. The second AI MUST return its findings as a VALID JSON object with exactly these keys and no other text, comments, or markdown formatting like \`\`\`json.
10. Make sure all fields are populated.
Return ONLY the text of the optimized prompt for the second AI. Do not include any explanations or conversational text in your own response.
Optimized Prompt for Second AI:
  `;

  logger.info("Meta-Prompting - Sending request to Gemini to generate main prompt. Meta-context:", metaContext);

  try {
    const result = await model.generateContent(metaPrompt);
    const response = result.response;
    const generatedPromptText = response?.candidates?.[0]?.content?.parts?.[0]?.text;

    if (!generatedPromptText || generatedPromptText.trim() === "") {
      logger.error("Meta-Prompting - Gemini returned empty or invalid prompt text.");
      throw new Error("Failed to generate dynamic prompt from meta-LLM call.");
    }
    logger.info("Meta-Prompting - Successfully generated dynamic prompt:", generatedPromptText);
    return generatedPromptText.trim();
  } catch (error: any) {
    logger.error("Meta-Prompting - Error calling Gemini to generate prompt:", error.message, error.stack);
    throw new HttpsError("internal", "Failed to generate dynamic prompt for AI: " + error.message);
  }
}


export const generatePostSuggestions = onCall<
  ClientRequestData,
  Promise<CloudFunctionSuccessResponse>
>(async (request) => {
  logger.info("generatePostSuggestions function called. Data received:", request.data);
  const localGenAI = initializeGenAI();

  const userText = request.data.text;
  if (!userText || typeof userText !== "string" || userText.trim() === "") {
    logger.error("Invalid input: 'text' field is missing or empty.");
    throw new HttpsError("invalid-argument", "Input 'text' is required.");
  }

  try {
    const dynamicPromptContent = await generateDynamicPromptForSuggestions(request.data);

    const finalPromptForGemini = `
${dynamicPromptContent}

User Request: "${userText}"

JSON Output:
    `;
    logger.info("Main Suggestion - Sending request to Gemini API. Model:", SUGGESTION_MODEL_NAME);
    logger.info("Main Suggestion - Final prompt being sent:", finalPromptForGemini);

    const model = localGenAI.getGenerativeModel({model: SUGGESTION_MODEL_NAME});
    const safetySettings: SafetySetting[] = [
      {category: HarmCategory.HARM_CATEGORY_HARASSMENT, threshold: HarmBlockThreshold.BLOCK_MEDIUM_AND_ABOVE},
      {category: HarmCategory.HARM_CATEGORY_HATE_SPEECH, threshold: HarmBlockThreshold.BLOCK_MEDIUM_AND_ABOVE},
      {category: HarmCategory.HARM_CATEGORY_SEXUALLY_EXPLICIT, threshold: HarmBlockThreshold.BLOCK_MEDIUM_AND_ABOVE},
      {category: HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT, threshold: HarmBlockThreshold.BLOCK_MEDIUM_AND_ABOVE},
    ];
    const generationConfig: GenerationConfig = {responseMimeType: "application/json"};
    const contentPart: Part = {text: finalPromptForGemini};
    const generateContentRequest: GenerateContentRequest = {
      contents: [{role: "user", parts: [contentPart]}],
      generationConfig,
      safetySettings,
    };

    const result: GenerateContentResult = await model.generateContent(generateContentRequest);
    const response = result.response;
    const candidate = response?.candidates?.[0];
    const responseText = candidate?.content?.parts?.[0]?.text;

    if (!responseText) {
      logger.error("Main Suggestion - Gemini API response candidate is missing content.", JSON.stringify(candidate));
      if (candidate?.finishReason && candidate.finishReason !== "STOP") {
        throw new HttpsError("internal", `Content generation stopped: ${candidate.finishReason}.`);
      }
      throw new HttpsError("internal", "AI service response content is invalid.");
    }
    logger.info("Main Suggestion - Raw JSON response text from Gemini:", responseText);

    let structuredData: LLMSuggestions;
    try {
      structuredData = JSON.parse(responseText.trim()) as LLMSuggestions;
      logger.info("Main Suggestion - Successfully parsed JSON from Gemini:", structuredData);
    } catch (error: any) {
      logger.error("Main Suggestion - Error parsing JSON. Raw text:", responseText, "Error:", error);
      const jsonMatch = responseText.match(/```json\s*([\s\S]*?)\s*```|({[\s\S]*})/);
      let extractedJsonText: string | null = null;
      if (jsonMatch) {
        extractedJsonText = jsonMatch[1] || jsonMatch[2];
        try {
          if (extractedJsonText) {
            structuredData = JSON.parse(extractedJsonText.trim()) as LLMSuggestions;
            logger.info("Main Suggestion - Successfully parsed JSON after extraction:", structuredData);
          } else {
            throw new Error("JSON match found but extracted text is null during fallback.");
          }
        } catch (innerError: any) {
          logger.error("Main Suggestion - Error parsing extracted JSON during fallback:", extractedJsonText, innerError);
          throw new HttpsError("internal", "Failed to parse response from AI (fallback). Raw: " + responseText);
        }
      } else {
        throw new HttpsError("internal", "Failed to parse response from AI (no JSON block). Raw: " + responseText);
      }
    }

    const requiredKeys: Array<keyof LLMSuggestions> = ["listingType", "suggestedTitle", "suggestedDescription", "extractedItemName", "suggestedCategories"];
    for (const key of requiredKeys) {
      if (!(key in structuredData) || (structuredData[key] === null && key !== "price" && key !== "currency" && key !== "suggestedCategories")) {
        logger.error(`Missing or null required key '${key}' in parsed JSON.`, structuredData);
        throw new HttpsError("internal", `AI service response missing or invalid key: ${key}`);
      }
    }

    const validListingTypes: ListingType[] = ["SELLING_ITEM", "WANTING_TO_BUY_ITEM", "WANTING_TO_OFFER_SERVICE", "OFFER_TO_BUY_SERVICE", "UNKNOWN"];
    if (!validListingTypes.includes(structuredData.listingType)) {
      logger.warn(`Invalid listingType '${structuredData.listingType}' from LLM, defaulting to UNKNOWN.`);
      structuredData.listingType = "UNKNOWN";
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
    logger.error("Error in generatePostSuggestions function:", error.message, error.stack, error.details ? JSON.stringify(error.details) : "");
    if (error instanceof HttpsError) {
      throw error;
    }
    throw new HttpsError("internal", "Unexpected error with AI service: " + error.message);
  }
});
