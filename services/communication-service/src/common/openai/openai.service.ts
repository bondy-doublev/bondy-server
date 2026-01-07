import { Injectable } from '@nestjs/common';
import OpenAI from 'openai';

@Injectable()
export class OpenaiService {
  private client: OpenAI;

  constructor() {
    this.client = new OpenAI({
      apiKey: process.env.OPENAI_API_KEY,
    });
  }

  embed(text: string) {
    return this.client.embeddings.create({
      model: 'text-embedding-3-small',
      input: text,
    });
  }

  chat(messages: OpenAI.Chat.ChatCompletionMessageParam[]) {
    return this.client.chat.completions.create({
      model: 'gpt-4o-mini',
      messages,
    });
  }
}
