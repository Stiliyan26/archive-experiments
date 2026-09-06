export interface NewsArticle  { 
  source: {
    id: string | null;
    name: string;
  };
  author: string | null;
  title: string | null;
  description: string | null;
  url: string;
  urlToImage: string | null;
  publishedAt: string;
  content: string | null;
}

export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  message?: string;
  error?: string;
}

export interface NewsApiResponse {
  status: string;
  totalResults: number;
  articles: NewsArticle[];
}

export interface TopHeadlinesQuery {
  country?: string;
  category?: string;
}

export interface SearchNewsQuery {
  q: string;
  from?: string;
  to?: string;
  sortBy?: 'relevancy' | 'popularity' | 'publishedAt';
}

export interface SummarizedNewsResponse extends NewsApiResponse {
  summary: string | null;
}