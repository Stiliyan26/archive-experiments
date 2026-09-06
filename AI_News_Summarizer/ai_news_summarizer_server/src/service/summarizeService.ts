import OpenAI from "openai";
import config from "../config/config.js";
import type { NewsArticle } from '../types/news.types.js';

const openAi = new OpenAI({
  apiKey: config.openAiApiKey
});

export async function summarizeNews(articles: NewsArticle[]): Promise<string | null> {

  const newsContent = articles
    .map((article, i) => `${i + 1}. ${article.title}\n${article.description || ''}`)
    .join('\n\n');

    const completion = await openAi.chat.completions.create({
       model: 'gpt-4o-mini',
       messages: [
          {
            role: 'system',
            content: 'You are a news summarizer. Provide a well-formatted summary with bullet points and clear sections.'
          },
          {
            role: 'user',
            content: `Summarize these news articles in a structured format with:\n- Main headlines\n- Key themes\n- Notable events\n\nArticles:\n\n${newsContent}`
          }
       ],
       max_tokens: 500
    });

    return completion.choices[0]?.message.content || null;
}