import OpenAI from 'openai';
export declare class OpenaiService {
    private client;
    constructor();
    embed(text: string): import("openai").APIPromise<OpenAI.Embeddings.CreateEmbeddingResponse>;
    chat(messages: OpenAI.Chat.ChatCompletionMessageParam[]): import("openai").APIPromise<OpenAI.Chat.Completions.ChatCompletion>;
}
