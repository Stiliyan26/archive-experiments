import dotenv from 'dotenv';

dotenv.config();

interface Config {
  port: number;
  nodeEnv: string;
  newsApiKey?: string | undefined;
  newsApiBaseUrl?: string;
  openAiApiKey?: string | undefined;
}

const config: Config = {
  port: parseInt(process.env.PORT || '3000', 10),
  nodeEnv: process.env.NODE_ENV || 'development',
  newsApiKey: process.env.NEWS_API_KEY,
  newsApiBaseUrl: 'https://newsapi.org/v2',
  openAiApiKey: process.env.OPEN_AI_API_KEY,
};

export default config;